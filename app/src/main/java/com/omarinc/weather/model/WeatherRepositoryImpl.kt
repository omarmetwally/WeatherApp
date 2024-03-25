package com.omarinc.weather.model

import com.omarinc.weather.db.WeatherLocalDataSource
import com.omarinc.weather.network.WeatherRemoteDataSource
import com.omarinc.weather.sharedpreferences.ISharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response

class WeatherRepositoryImpl(
    private val weatherRemoteDataSource: WeatherRemoteDataSource,
    private val weatherLocalDataSource: WeatherLocalDataSource,
    private val settingSharedPreferences: ISharedPreferences
) : WeatherRepository {

    companion object {

        @Volatile
        private var instance: WeatherRepositoryImpl? = null

        fun getInstance(
            weatherRemoteDataSource: WeatherRemoteDataSource,
            weatherLocalDataSource: WeatherLocalDataSource,
            settingSharedPreferences:ISharedPreferences
        ): WeatherRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: WeatherRepositoryImpl(weatherRemoteDataSource,weatherLocalDataSource,settingSharedPreferences).also { instance = it }
            }
        }
    }

    override suspend fun getWeatherResponse(
        coordinate: Coordinates,
        language: String,  units:String
    ): Flow<Response<WeatherResponse>> {

        return withContext(Dispatchers.IO){
            weatherRemoteDataSource.getWeatherResponse(coordinate,language,units)
        }
    }

    override suspend fun insertFavorite(city: FavoriteCity) {
        withContext(Dispatchers.IO)
        {
            weatherLocalDataSource.insert(city)
        }
    }

    override suspend fun deleteFavorite(city: FavoriteCity) {
        withContext(Dispatchers.IO)
        {
            weatherLocalDataSource.delete(city)
        }
    }

    override suspend fun getAllFavorites(): Flow<List<FavoriteCity>> {
        return withContext(Dispatchers.IO){
            weatherLocalDataSource.getFavoriteCities()
        }
    }

    override fun writeStringToSharedPreferences(key: String, value: String) {
        settingSharedPreferences.writeStringToSharedPreferences(key,value)
    }

    override fun readStringFromSharedPreferences(key: String): String {
       return settingSharedPreferences.readStringFromSharedPreferences(key)
    }


    override suspend fun insertAlert(alert: WeatherAlert) {
        weatherLocalDataSource.insertAlert(alert)
    }

    override fun getAllAlerts(): Flow<List<WeatherAlert>> {
        return weatherLocalDataSource.getAllAlerts()
    }



}