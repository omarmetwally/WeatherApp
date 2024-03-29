package com.omarinc.weather.model

import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface WeatherRepository {
    suspend fun getWeatherResponse(
        coordinate: Coordinates,
        language: String,
        units:String
    ): Flow<Response<WeatherResponse>>


    suspend fun insertFavorite(city:FavoriteCity)
    suspend fun deleteFavorite(city:FavoriteCity)
    suspend fun getAllFavorites():Flow<List<FavoriteCity>>

    fun writeStringToSharedPreferences(key: String, value: String)
    fun readStringFromSharedPreferences(key: String): String


    fun writeCoordinatesToSharedPreferences(key: String, value: Double)
    fun readSCoordinatesFromSharedPreferences(key: String): Double


     suspend fun insertAlert(alert: WeatherAlert)
    suspend fun deleteAlert(alert: WeatherAlert)

    fun getAllAlerts(): Flow<List<WeatherAlert>>


    suspend fun insertCashedData(weatherResponse: WeatherResponse)

    suspend fun deleteCashedData()

    fun getCashedData(): Flow<WeatherResponse>?


}