package com.omarinc.weather.currentHomeWeather.viewmodel

import android.content.Context
import android.location.Location
import android.os.Looper
import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.DailyForecastUI
import com.omarinc.weather.model.DailyWeather
import com.omarinc.weather.model.ForecastEntry
import com.omarinc.weather.model.TodayForecastUI
import com.omarinc.weather.model.WeatherRepository
import com.omarinc.weather.model.WeatherResponse
import com.omarinc.weather.network.ApiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CurrentWeatherViewModel(private val _repo: WeatherRepository, val context: Context) :
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


    private val _fiveDayForecast = MutableStateFlow<List<DailyForecastUI>>(emptyList())
    val fiveDayForecast: StateFlow<List<DailyForecastUI>> = _fiveDayForecast

    private val _todayForecast = MutableStateFlow<List<TodayForecastUI>>(emptyList())
    val todayForecast: StateFlow<List<TodayForecastUI>> = _todayForecast

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

                DailyForecastUI(dayName, temperatureRange, mostCommonCondition, icon)
            }
            _fiveDayForecast.value = dailyForecasts.take(5)
        }
    }



//    fun extractTodayForecast(forecasts: List<ForecastEntry>) {
//        viewModelScope.launch {
//            val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//            val todayForecasts = forecasts.filter { it.dt_txt.startsWith(todayString) }.map {
//                TodayForecastUI(
//                    it.dt_txt.substring(11, 16),
//                    it.main.temp,
//                    it.weather.first().description,
//                    it.weather.first().icon
//                )
//            }
//            _todayForecast.value = todayForecasts
//        }
//    }

    fun extractTodayForecast(forecasts: List<ForecastEntry>) {
        viewModelScope.launch {
            val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val todayForecasts = forecasts.filter { it.dt_txt.startsWith(todayString) }.map {
                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(it.dt_txt.substring(11, 16))
                val formattedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(time)
                TodayForecastUI(
                    formattedTime,
                    it.main.temp,
                    it.weather.first().description,
                    it.weather.first().icon
                )
            }
            _todayForecast.value = todayForecasts
        }
    }

}