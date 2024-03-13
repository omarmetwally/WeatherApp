package com.omarinc.weather.model

import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface WeatherRepository {
    suspend fun getWeatherResponse(
        coordinate: Coordinates,
        language: String
    ): Flow<Response<WeatherResponse>>
}