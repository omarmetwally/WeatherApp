package com.omarinc.weather.network

import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.WeatherResponse

sealed class ApiState {
    class Success(val weatherResponse: WeatherResponse):ApiState()
    class Failure(val msg:Throwable):ApiState()
    object Loading :ApiState()

}
sealed class DataBaseState <out T> {
    class Success<out T>(val data: List<T>): DataBaseState<T>()
    class Failure(val msg: Throwable): DataBaseState<Nothing>()
    object Loading : DataBaseState<Nothing>()

}