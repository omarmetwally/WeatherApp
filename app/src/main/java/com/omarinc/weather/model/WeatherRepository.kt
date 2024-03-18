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



}