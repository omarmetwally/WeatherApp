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


}