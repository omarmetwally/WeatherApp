package com.omarinc.weather.currentHomeWeather.viewmodel

import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.DailyForecast
import com.omarinc.weather.model.ForecastEntry
import com.omarinc.weather.model.TodayForecast
import com.omarinc.weather.model.WeatherRepository
import com.omarinc.weather.network.ApiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeViewModel (private val _repo: WeatherRepository, val context: Context) :
    ViewModel() {
    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context.applicationContext)

    private val _weather = MutableStateFlow<ApiState>(ApiState.Loading)
    val weather: StateFlow<ApiState> = _weather


    fun getCurrentWeather(
        coordinate: Coordinates,
        language: String,
        units:String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.getWeatherResponse(coordinate, language,units)
                .catch { e ->
                    _weather.value = ApiState.Failure(e)
                }
                .collect() { data ->
                    _weather.value = ApiState.Success(data.body()!!)
                }
        }
    }

    init {
        startLocationUpdates()
    }

    @SuppressWarnings("MissingPermission")
    fun startLocationUpdates() {
        var locationCallback: LocationCallback
        var coordinates: Coordinates = Coordinates(0.0, 0.0)

        val locationRequest = LocationRequest
            .create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                val location: Location? = p0.lastLocation


                if (location != null) {


                    coordinates.lat = location.latitude
                    coordinates.lon = location.longitude

                    getCurrentWeather(coordinates, "en","metric")


                }

            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()!!
        )

    }


    fun onLocationUpdated(coordinate: Coordinates) {
        getCurrentWeather(coordinate, "en","metric")
    }


    private val _fiveDayForecast = MutableStateFlow<List<DailyForecast>>(emptyList())
    val fiveDayForecast: StateFlow<List<DailyForecast>> = _fiveDayForecast

    private val _todayForecast = MutableStateFlow<List<TodayForecast>>(emptyList())
    val todayForecast: StateFlow<List<TodayForecast>> = _todayForecast

//    fun extractFiveDayForecast(forecasts: List<ForecastEntry>) {
//        viewModelScope.launch {
//            val groupedByDay = forecasts.groupBy { it.dt_txt.substring(0, 10) }
//            val dailyForecasts = groupedByDay.map { entry ->
//                val averageTemp = entry.value.sumOf { it.main.temp } / entry.value.size
//                val mostCommonCondition = entry.value.groupBy { it.weather.first().main }
//                    .maxByOrNull { it.value.size }?.key ?: ""
//                val icon = entry.value.first().weather.first().icon
//                DailyForecastUI(entry.key, averageTemp, mostCommonCondition, icon)
//            }
//            _fiveDayForecast.value = dailyForecasts.take(5)
//        }
//    }


    fun extractFiveDayForecast(forecasts: List<ForecastEntry>) {
        viewModelScope.launch {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val groupedByDay = forecasts.groupBy { it.dt_txt.substring(0, 10) }.entries.toList()
            val dailyForecasts = groupedByDay.mapIndexed { index, entry ->
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(entry.key)!!
                val dayName = if (index == 0) "Today" else SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
                val tempMax = entry.value.maxOf { it.main.temp_max }
                val tempMin = entry.value.minOf { it.main.temp_min }
                val temperatureRange = "${tempMax.toInt()}/${tempMin.toInt()}"
                val mostCommonCondition = entry.value.groupBy { it.weather.first().main }
                    .maxByOrNull { it.value.size }?.key ?: ""
                val icon = entry.value.first().weather.first().icon
                DailyForecast(dayName, temperatureRange, mostCommonCondition, icon)
            }
            _fiveDayForecast.value = dailyForecasts.take(5)
        }
    }





    /*
       //function to get only 3hr in the current day
       fun extractTodayForecasts(forecasts: List<ForecastEntry>) {
           viewModelScope.launch {
               val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
               val todayForecasts = forecasts.filter { it.dt_txt.startsWith(todayString) }.map {
                   val time = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(it.dt_txt.substring(11, 16))
                   val formattedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(time)
                   TodayForecast(
                       formattedTime,
                       it.main.temp,
                       it.weather.first().description,
                       it.weather.first().icon
                   )
               }
               _todayForecast.value = todayForecasts
           }
       }*/

    fun extractTodayForecast(forecasts: List<ForecastEntry>) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val todayString = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val nextDayString = dateFormat.format(calendar.time)
            val todayAndNextDayForecasts = forecasts.filter {
                val dateText = it.dt_txt.substring(0, 10)
                dateText == todayString || dateText == nextDayString
            }.map {
                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(it.dt_txt.substring(11, 16))
                val formattedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(time!!)
                TodayForecast(
                    time = formattedTime,
                    temp = it.main.temp,
                    condition = it.weather.first().description,
                    icon = it.weather.first().icon
                )
            }
            _todayForecast.value = todayAndNextDayForecasts
        }
    }

    fun extractCurrentForecast(): TodayForecast
    {
        return _todayForecast.value.get(0)
    }

}