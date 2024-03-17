package com.omarinc.weather.favorite.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.omarinc.weather.model.WeatherRepository

class FavoriteViewModel  (private val _repo: WeatherRepository, val context: Context) :
    ViewModel(){

}