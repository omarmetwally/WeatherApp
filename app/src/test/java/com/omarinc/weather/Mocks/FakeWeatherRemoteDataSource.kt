package com.omarinc.weather.Mocks

import com.omarinc.weather.model.CityInfo
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.WeatherResponse
import com.omarinc.weather.network.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class FakeWeatherRemoteDataSource(var response: Response<WeatherResponse>? = null) : WeatherRemoteDataSource {
    override fun getWeatherResponse(coordinate: Coordinates, language: String, units: String): Flow<Response<WeatherResponse>> = flow {
        emit(response ?: Response.success(WeatherResponse("200", 0, 0, emptyList(), CityInfo(0, "", Coordinates(0.0, 0.0), "", 0, 0, 0, 0))))
    }
}
