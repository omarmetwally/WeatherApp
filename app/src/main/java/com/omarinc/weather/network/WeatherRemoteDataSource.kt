package com.omarinc.weather.network

import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface WeatherRemoteDataSource {
     fun getWeatherResponse(coordinate: Coordinates, language: String, units:String): Flow<Response<WeatherResponse>>
}