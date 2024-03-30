package com.omarinc.weather.Mocks

import com.omarinc.weather.sharedpreferences.ISharedPreferences

class FakeSharedPreferences : ISharedPreferences {
    private val storage = mutableMapOf<String, Any>()

    override fun writeStringToSharedPreferences(key: String, value: String) {
        storage[key] = value
    }

    override fun readStringFromSharedPreferences(key: String): String = storage[key] as? String ?: ""

    override fun writeCoordinatesToSharedPreferences(key: String, value: Double) {
        storage[key] = value
    }

    override fun readCoordinatesFromSharedPreferences(key: String): Double = storage[key] as? Double ?: 0.0
}
