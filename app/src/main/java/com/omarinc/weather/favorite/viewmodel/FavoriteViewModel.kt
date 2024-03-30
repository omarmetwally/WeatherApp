package com.omarinc.weather.favorite.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.WeatherRepository
import com.omarinc.weather.network.ApiState
import com.omarinc.weather.network.DataBaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FavoriteViewModel  (private val _repo: WeatherRepository, val context: Context) :
    ViewModel(){
//
//        private var _favorite:MutableLiveData<List<FavoriteCity>> = MutableLiveData<List<FavoriteCity>>()
//        val favorite:Liva

        private var _favorite = MutableStateFlow<List<FavoriteCity>>(emptyList())
        val favorite: StateFlow<List<FavoriteCity>> = _favorite

        fun insertFavorite(city:FavoriteCity)
        {
            viewModelScope.launch(Dispatchers.IO) {
                _repo.insertFavorite(city)
                getAllFavorites()

            }
        }
        fun deleteFavorite(city: FavoriteCity)
        {
            viewModelScope.launch(Dispatchers.IO) {
                _repo.deleteFavorite(city)
                getAllFavorites()

            }
        }

        fun getAllFavorites(){
            viewModelScope.launch(Dispatchers.IO) {

                _repo.getAllFavorites().catch {
//                    e->_favorite.value=e
                }
                    .collect{data->
                        _favorite.value=data
                    }
            }
        }


}