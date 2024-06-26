package com.omarinc.weather.currentHomeWeather.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.omarinc.weather.alert.viewmodel.AlertViewModel
import com.omarinc.weather.favorite.viewmodel.FavoriteViewModel
import com.omarinc.weather.model.WeatherRepository
import com.omarinc.weather.settings.SettingViewModel

class ViewModelFactory (private val _repo:WeatherRepository
       ,val context: Context):ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(HomeViewModel::class.java)){
            return HomeViewModel(_repo, context) as T
        }
        else if(modelClass.isAssignableFrom(FavoriteViewModel::class.java)){
            return FavoriteViewModel(_repo, context) as T
        }
        else if(modelClass.isAssignableFrom(SettingViewModel::class.java)){
            return SettingViewModel(_repo, context) as T
        }else if(modelClass.isAssignableFrom(AlertViewModel::class.java)){
            return AlertViewModel(_repo, context) as T
        }
        throw IllegalArgumentException("ViewModel Class not found")
    }
}