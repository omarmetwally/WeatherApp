package com.omarinc.weather.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.omarinc.weather.model.WeatherRepository

class SettingViewModel (private val _repo: WeatherRepository, val context: Context) : ViewModel() {

    fun writeStringToSharedPreferences(key: String, value: String) {
        _repo.writeStringToSharedPreferences(key, value)
    }

    fun readStringFromSharedPreferences(key: String): String {
        return _repo.readStringFromSharedPreferences(key)
    }

    fun writeCoordinatesToSharedPreferences(key: String, value: Double) {
        _repo.writeCoordinatesToSharedPreferences(key, value)
    }

    fun readCoordinatesFromSharedPreferences(key: String): Double {
        return _repo.readSCoordinatesFromSharedPreferences(key)
    }


}