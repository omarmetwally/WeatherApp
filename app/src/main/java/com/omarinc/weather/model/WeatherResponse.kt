package com.omarinc.weather.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weatherResponse")
data class WeatherResponse( val cod: String,
                            val message: Int,
                            val cnt: Int,
                            val list: List<ForecastEntry>,
                           @PrimaryKey val city: CityInfo
)
