package com.omarinc.weather.currentHomeWeather.viewmodel

import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
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
import com.omarinc.weather.model.WeatherResponse
import com.omarinc.weather.network.ApiState
import com.omarinc.weather.network.DataBaseState
import com.omarinc.weather.utilities.Constants
import com.omarinc.weather.utilities.Helpers
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

    private var coordinates: Coordinates = Coordinates(0.0, 0.0)

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
    fun getCoordinates():Coordinates{
        return coordinates
    } fun setCoordinates(coordinate: Coordinates){
         coordinates=coordinate
    }
    private val _fiveDayForecast = MutableStateFlow<List<DailyForecast>>(emptyList())
    val fiveDayForecast: StateFlow<List<DailyForecast>> = _fiveDayForecast

    private val _todayForecast = MutableStateFlow<List<TodayForecast>>(emptyList())
    val todayForecast: StateFlow<List<TodayForecast>> = _todayForecast

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
                val temperatureRange = "${numberFormat.format(convertUnit(tempMax.toInt()) )}/${numberFormat.format(convertUnit(tempMin.toInt()))} ${Helpers.decideUnit(_repo.readStringFromSharedPreferences(Constants.KEY_TEMPERATURE_UNIT),context)}"
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
               val formattedTemp = numberFormat.format(convertUnit(it.main.temp.toInt())) + Helpers.decideUnit(_repo.readStringFromSharedPreferences(Constants.KEY_TEMPERATURE_UNIT),context)
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

    fun convertUnit(temp:Int):Int{
        if(_repo.readStringFromSharedPreferences(Constants.KEY_TEMPERATURE_UNIT)==Constants.VALUE_FAHRENHEIT)
        {
            return Helpers.celsiusToFahrenheit(temp)
        }
        else if (_repo.readStringFromSharedPreferences(Constants.KEY_TEMPERATURE_UNIT)==Constants.VALUE_KELVIN)
        {
            return Helpers.celsiusToKelvin(temp)
        }
        return temp
    }

    fun extractCurrentForecast(): TodayForecast
    {
        return _todayForecast.value.get(0)
    }




    private var _weatherDB=MutableStateFlow<DataBaseState<WeatherResponse>>(DataBaseState.Loading)
    val weatherDB:StateFlow<DataBaseState<WeatherResponse>> = _weatherDB



    fun insertCashedData(weatherResponse: WeatherResponse)
    {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.insertCashedData(weatherResponse)
        }
    }
    fun deleteCashedData()
    {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.deleteCashedData()
        }
    }

    fun getCashedData(){
        viewModelScope.launch(Dispatchers.IO) {

            _repo.getCashedData().catch {
                    e->_weatherDB.value=DataBaseState.Failure(e)
            }
                .collect{data->
                    _weatherDB.value=DataBaseState.SuccessObj(data)
                }
        }
    }



    fun writeCityToSharedPreferences(key: String, value: String) {
        _repo.writeStringToSharedPreferences(key, value)
    }

    fun readCityFromSharedPreferences(key: String): String {
        return _repo.readStringFromSharedPreferences(key)
    }


}