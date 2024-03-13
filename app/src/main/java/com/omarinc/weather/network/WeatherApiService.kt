package com.omarinc.weather.network

import com.omarinc.weather.model.WeatherResponse
import com.omarinc.weather.utilities.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("forecast?appid=${Constants.API_KEY}")
    suspend fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("lang") language: String,
        @Query("units") units:String
    ): Response<WeatherResponse>
}