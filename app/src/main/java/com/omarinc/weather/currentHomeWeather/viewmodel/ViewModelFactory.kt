package com.omarinc.weather.currentHomeWeather.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.omarinc.weather.model.WeatherRepository

class ViewModelFactory (private val _repo:WeatherRepository
       ,val context: Context):ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(CurrentWeatherViewModel::class.java)){
            return CurrentWeatherViewModel(_repo, context) as T
        }
        throw IllegalArgumentException("ViewModel Class not found")
    }
}