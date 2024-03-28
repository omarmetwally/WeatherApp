package com.omarinc.weather.model

data class CityInfo(
    val id: Int,
    var name: String,
    val coord: Coordinates,
    var country: String,
    val population: Int,
    val timezone: Int,
    val sunrise: Long,
    val sunset: Long
)

data class Coordinates(
    var lat: Double,
    var lon: Double
)
