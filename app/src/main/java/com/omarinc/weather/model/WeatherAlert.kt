package com.omarinc.weather.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weatheralert")
data class WeatherAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val alertTime: Long
)