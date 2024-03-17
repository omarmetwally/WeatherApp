package com.omarinc.weather.db

import com.omarinc.weather.model.FavoriteCity
import kotlinx.coroutines.flow.Flow

interface WeatherLocalDataSource {
    suspend fun insert(city:FavoriteCity)
    suspend fun delete(city:FavoriteCity)
    suspend fun getFavoriteCities(): Flow<List<FavoriteCity>>
}