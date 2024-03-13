package com.omarinc.weather.currentHomeWeather.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.WeatherRepository
import com.omarinc.weather.model.WeatherResponse
import com.omarinc.weather.network.ApiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CurrentWeatherViewModel (private val _repo:WeatherRepository):ViewModel(){
    private val _weather=MutableStateFlow<ApiState>(ApiState.Loading)
    val weather:StateFlow<ApiState> = _weather


    fun getCurrentWeather(coordinate: Coordinates,
                          language: String)
    {
        viewModelScope.launch (Dispatchers.IO){
            _repo.getWeatherResponse(coordinate,language)
                .catch { e->
                    _weather.value=ApiState.Failure(e)
                }
                .collect() {data->
                    _weather.value=ApiState.Success(data.body()!!)
                }
        }
    }

}