package com.omarinc.weather.model

import com.omarinc.weather.network.WeatherRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.Response

class WeatherRepositoryImpl(
    private val weatherRemoteDataSource: WeatherRemoteDataSource
) : WeatherRepository {

    companion object {

        @Volatile
        private var instance: WeatherRepositoryImpl? = null

        fun getInstance(
            weatherRemoteDataSource: WeatherRemoteDataSource
        ): WeatherRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: WeatherRepositoryImpl(weatherRemoteDataSource).also { instance = it }
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
}