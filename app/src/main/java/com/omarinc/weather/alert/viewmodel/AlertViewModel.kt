package com.omarinc.weather.alert.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.WeatherAlert
import com.omarinc.weather.model.WeatherRepository
import com.omarinc.weather.network.ApiState
import com.omarinc.weather.network.DataBaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AlertViewModel  (private val _repo: WeatherRepository, val context: Context) :
    ViewModel(){



    private  var _alert= MutableStateFlow<DataBaseState<WeatherAlert>>(DataBaseState.Loading)
    val alert: StateFlow<DataBaseState<WeatherAlert>> = _alert

    fun insertAlert(alert: WeatherAlert)
    {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.insertAlert(alert)
        }
    }
//    fun deleteFavorite(alert: WeatherAlert)
//    {
//        viewModelScope.launch(Dispatchers.IO) {
//            _repo.(alert)
//        }
//    }

    fun getAllAlerts(){
        viewModelScope.launch(Dispatchers.IO) {

            _repo.getAllAlerts().catch {
                    e->_alert.value=DataBaseState.Failure(e)
            }
                .collect{data->
                    _alert.value=DataBaseState.Success(data)
                }
        }
    }




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
}