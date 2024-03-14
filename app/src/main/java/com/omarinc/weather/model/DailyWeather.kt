package com.omarinc.weather.model

data class DailyWeather(
    val id: Long,
//    val day: String,
//    val status: String,
//    val icon: Int,
//    val temperature: String
    val date: String,
    val averageTemp: Double,
    val condition: String,
    val iconUrl: String
)

data class DailyForecast(
    val date: String,
    val averageTemp: String,
    val condition: String,
    val icon: String
)

data class TodayForecast(
    val time: String,
    val temp: Double,
    val condition: String,
    val icon: String
)
