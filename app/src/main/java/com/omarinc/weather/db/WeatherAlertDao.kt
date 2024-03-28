package com.omarinc.weather.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.WeatherAlert
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherAlertDao {
    @Insert
    suspend fun insertAlert(weatherAlert: WeatherAlert)

    @Query("SELECT * FROM weatheralert")
    fun getAllAlerts(): Flow<List<WeatherAlert>>

    @Delete
    suspend fun deleteAlert(weatherAlert: WeatherAlert)
}