package com.omarinc.weather.sharedpreferences

interface ISharedPreferences {
    fun writeStringToSharedPreferences(key: String, value: String)
    fun readStringFromSharedPreferences(key: String): String


    fun writeCoordinatesToSharedPreferences(key: String, value: Double)
    fun readCoordinatesFromSharedPreferences(key: String): Double
}