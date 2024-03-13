package com.omarinc.weather.model

data class CityInfo(
    val id: Int,
    val name: String,
    val coord: Coordinates,
    val country: String,
    val population: Int,
    val timezone: Int,
    val sunrise: Long,
    val sunset: Long
)

data class Coordinates(
    var lat: Double,
    var lon: Double
)
