package com.omarinc.weather.model

import com.omarinc.weather.network.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class WeatherRepositoryImpl (
    private val weatherRemoteDataSource:WeatherRemoteDataSource
) :WeatherRepository{

//    companion object{
//        private var instance
//    }

    override suspend fun getWeatherResponse(
        coordinate: Coordinates,
        language: String
    ): Flow<Response<WeatherResponse>> {
        TODO("Not yet implemented")
    }
}