package com.omarinc.weather.network

import com.omarinc.weather.model.WeatherResponse

sealed class ApiState {
    class Success(val weatherResponse: WeatherResponse):ApiState()
    class Failure(val msg:Throwable):ApiState()
    object Loading :ApiState()

}