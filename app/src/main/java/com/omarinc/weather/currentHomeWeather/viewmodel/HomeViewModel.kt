package com.omarinc.weather.currentHomeWeather.viewmodel

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.omarinc.weather.R
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
import java.text.NumberFormat
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

    private  val TAG = "HomeViewModel"

    fun getCurrentWeather(
        coordinate: Coordinates,
        language: String,
        units:String
    ) {
        Log.i(TAG, "zzzzzzzzzzzzzzzzzzzzz: $units")
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

    var coordinates: Coordinates = Coordinates(0.0, 0.0)

    @SuppressWarnings("MissingPermission")
    fun startLocationUpdates(language: String,units: String) {
        var locationCallback: LocationCallback

        Log.i(TAG, "sssssssssssssssssssss: $units")
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

                    getCurrentWeather(coordinates, language,if (units == "null") "metric" else units)
                }

            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()!!
        )

    }

    lateinit var cityName:String
    fun getCityName():Coordinates{
        return coordinates
    }

//    fun onLocationUpdated(coordinate: Coordinates) {
//        getCurrentWeather(coordinate, "en","metric")
//    }


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
            val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault()) // Get the number format for the current locale
            val dailyForecasts = groupedByDay.mapIndexed { index, entry ->
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(entry.key)!!
                val dayName = if (index == 0) context.getString(R.string.today) else SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
                val tempMax = entry.value.maxOf { it.main.temp_max }
                val tempMin = entry.value.minOf { it.main.temp_min }
                // Format temperature range using the locale-specific number format
                val temperatureRange = "${numberFormat.format(tempMax.toInt())}/${numberFormat.format(tempMin.toInt())}"
                val conditionOccurrences = entry.value.groupBy { it.weather.first().description }
                val mostCommonCondition = conditionOccurrences.maxByOrNull { it.value.size }?.key ?: ""
                val icon = conditionOccurrences.maxByOrNull { it.value.size }?.value?.first()?.weather?.first()?.icon ?: ""

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
           val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
           val calendar = Calendar.getInstance()
           val todayString = dateFormat.format(calendar.time)
           val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
           Log.i(TAG, "todayString: $todayString")
           calendar.add(Calendar.DAY_OF_YEAR, 1)
           val nextDayString = dateFormat.format(calendar.time)

           val todayAndNextDayForecasts = forecasts.filter {
               val dateText = it.dt_txt.substring(0, 10)
               dateText == todayString || dateText == nextDayString
           }.map {
               val time = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(it.dt_txt.substring(11, 16))
               val formattedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(time!!)
               val formattedTemp = numberFormat.format(it.main.temp.toInt())
               TodayForecast(
                   time = formattedTime,
                   temp = formattedTemp,
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