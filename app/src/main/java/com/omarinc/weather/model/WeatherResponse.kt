package com.omarinc.weather.model

import androidx.room.Entity

@Entity
data class WeatherResponse( val cod: String,
                            val message: Int,
                            val cnt: Int,
                            val list: List<ForecastEntry>,
                            val city: CityInfo)
