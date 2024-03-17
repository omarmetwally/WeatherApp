package com.omarinc.weather.network

import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.WeatherResponse

sealed class ApiState {
    class Success(val weatherResponse: WeatherResponse):ApiState()
    class Failure(val msg:Throwable):ApiState()
    object Loading :ApiState()

}
sealed class DataBaseState {
    class Success(val favoriteCity: List<FavoriteCity>):DataBaseState()
    class Failure(val msg:Throwable):DataBaseState()
    object Loading :DataBaseState()

}