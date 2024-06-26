package com.omarinc.weather.db

import android.content.Context
import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.WeatherAlert
import com.omarinc.weather.model.WeatherResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherLocalDataSourceImpl private constructor(context: Context):WeatherLocalDataSource{
    private val favoriteDao:FavouriteDao=WeatherDB.getInstance(context).getDao()
    private val alertDao:WeatherAlertDao=WeatherDB.getInstance(context).getAlertDao()
    private val weatherDao:WeatherDao=WeatherDB.getInstance(context).getWeatherDao()
    private  val TAG = "WeatherLocalDataSourceI"


    companion object {
        @Volatile
        private var instance: WeatherLocalDataSourceImpl? = null

        fun getInstance(context: Context): WeatherLocalDataSourceImpl {
            return instance ?: synchronized(this) {
                instance ?: WeatherLocalDataSourceImpl(context).also { instance = it }
            }
        }
    }

    override suspend fun insert(city: FavoriteCity) {
        CoroutineScope(Dispatchers.IO).launch {
            favoriteDao.insertFavoriteCity(city)
        }
    }

    override suspend fun delete(city: FavoriteCity) {
        CoroutineScope(Dispatchers.IO).launch {
            favoriteDao.deleteFavoriteCity(city)
        }
    }

    override suspend fun getFavoriteCities(): Flow<List<FavoriteCity>> = withContext(Dispatchers.IO){
        val favorites = favoriteDao.getAllFavouriteCites()
        favorites
    }

    override suspend fun insertAlert(alert: WeatherAlert) {
        CoroutineScope(Dispatchers.IO).launch {
            alertDao.insertAlert(alert)
        }
    }

    override suspend fun deleteAlert(alert: WeatherAlert) {

        CoroutineScope(Dispatchers.IO).launch {
            alertDao.deleteAlert(alert)
        }

    }

    override fun getAllAlerts(): Flow<List<WeatherAlert>> = alertDao.getAllAlerts()
    override suspend fun insertCashedData(weatherResponse: WeatherResponse) {
        CoroutineScope(Dispatchers.IO).launch {
            weatherDao.insertCashedData(weatherResponse)
        }
    }

    override suspend fun deleteCashedData() {
        CoroutineScope(Dispatchers.IO).launch {
            weatherDao.deleteCashedData()
        }
    }

    override fun getCashedData(): Flow<WeatherResponse> = weatherDao.getCashedData()


}