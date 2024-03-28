package com.omarinc.weather.db

import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.WeatherAlert
import com.omarinc.weather.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

interface WeatherLocalDataSource {
    suspend fun insert(city:FavoriteCity)
    suspend fun delete(city:FavoriteCity)
    suspend fun getFavoriteCities(): Flow<List<FavoriteCity>>


     suspend fun insertAlert(alert: WeatherAlert)
     fun getAllAlerts(): Flow<List<WeatherAlert>>


    suspend fun insertCashedData(weatherResponse: WeatherResponse)

    suspend fun deleteCashedData()

    fun getCashedData(): Flow<WeatherResponse>
}