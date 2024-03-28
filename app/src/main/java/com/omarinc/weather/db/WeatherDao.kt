package com.omarinc.weather.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omarinc.weather.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashedData(weatherResponse: WeatherResponse)

    @Query("DELETE FROM weatherResponse")
    suspend fun deleteCashedData()

    @Query("SELECT * FROM weatherResponse")
    fun getCashedData(): Flow<WeatherResponse>
}