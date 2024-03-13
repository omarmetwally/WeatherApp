package com.omarinc.weather.network

import android.content.Context
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.WeatherResponse
import com.omarinc.weather.utilities.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRemoteDataSourceImpl private constructor(private val context: Context):WeatherRemoteDataSource{
   private val weatherService :WeatherApiService


    companion object {

        @Volatile
        private var instance: WeatherRemoteDataSourceImpl? = null

        fun getInstance(context: Context): WeatherRemoteDataSourceImpl =
            instance ?: synchronized(this) {
                instance ?: WeatherRemoteDataSourceImpl(context).also { instance = it }
            }
    }

   init {

       val retrofit = Retrofit.Builder()
           .baseUrl(Constants.BASE_URL)
           .addConverterFactory(GsonConverterFactory.create())
           .build()
       weatherService=retrofit.create(WeatherApiService::class.java)
   }

    override  fun getWeatherResponse(
        coordinate: Coordinates,
        language: String
    ): Flow<Response<WeatherResponse>> = flow {
        try {
            val response = weatherService.getForecast(coordinate.lat,coordinate.lon,language)
            if (response.isSuccessful && response.body() != null) {
                emit(response)
            } else {
                throw HttpException(response)
            }
        } catch (e: Exception) {
            throw e
        }
    }

}