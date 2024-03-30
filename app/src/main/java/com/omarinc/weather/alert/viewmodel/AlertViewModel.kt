package com.omarinc.weather.alert.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.WeatherAlert
import com.omarinc.weather.model.WeatherRepository
import com.omarinc.weather.network.ApiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlertViewModel  (private val _repo: WeatherRepository, val context: Context) :
    ViewModel(){



    private var _alert = MutableStateFlow<List<WeatherAlert>>(emptyList())
    val alert: StateFlow<List<WeatherAlert>> = _alert


    init {
        getAllAlerts()
    }
    fun insertAlert(alert: WeatherAlert)
    {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.insertAlert(alert)
        }
    }
    fun deleteAlert(alert: WeatherAlert)
    {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.deleteAlert(alert)
        }
    }

    fun getAllAlerts(){
        viewModelScope.launch(Dispatchers.IO) {

            _repo.getAllAlerts().catch {
                    e-> Log.e("AlertViewModel", "Failed to fetch alerts: ${e.message}")

            }
                .collectLatest{data->
                    _alert.value=data
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


    fun toggleAlertNotificationState(alertId: Int, isEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.toggleAlertNotificationState(alertId, isEnabled)
//            getAllAlerts()
        }
    }

}