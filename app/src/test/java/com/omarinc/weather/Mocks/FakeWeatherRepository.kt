package com.omarinc.weather.Mocks

import android.util.Log
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.WeatherAlert
import com.omarinc.weather.model.WeatherRepository
import com.omarinc.weather.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import retrofit2.Response


class FakeWeatherRepository : WeatherRepository {

    var response: Response<WeatherResponse>? = null
    private  val TAG = "FakeWeatherRepository"

    override suspend fun getWeatherResponse(coordinate: Coordinates, language: String, units: String): Flow<Response<WeatherResponse>> = flow {
        response?.let { emit(it) } ?: error("WeatherResponse not set in FakeWeatherRepository")
    }

    private val _favorites = MutableStateFlow<List<FavoriteCity>>(emptyList())

    override suspend fun insertFavorite(city: FavoriteCity) {
//        val updatedFavorite = _favorites.value.toMutableList().apply { add(city) }
//        _favorites.emit(updatedFavorite)
        _favorites.value= listOf(city)

    }

    override suspend fun deleteFavorite(city: FavoriteCity) {
        val updatedFavorite = _favorites.value.toMutableList().apply { remove(city) }
        _favorites.emit(updatedFavorite)    }

    override suspend fun getAllFavorites(): Flow<List<FavoriteCity>> = _favorites

    override fun writeStringToSharedPreferences(key: String, value: String) {
        TODO("Not yet implemented")
    }

    override fun readStringFromSharedPreferences(key: String): String {
        TODO("Not yet implemented")
    }

    override fun writeCoordinatesToSharedPreferences(key: String, value: Double) {
        TODO("Not yet implemented")
    }

    override fun readSCoordinatesFromSharedPreferences(key: String): Double {
        TODO("Not yet implemented")
    }


    private val _alerts = MutableStateFlow<List<WeatherAlert>>(emptyList())

    override suspend fun insertAlert(alert: WeatherAlert) {
//        val updatedAlerts = _alerts.value.toMutableList().apply { add(alert) }
//        _alerts.emit(updatedAlerts)
        _alerts.value= listOf(alert)

        Log.i(TAG, "insertAlert: ")
    }

    override suspend fun deleteAlert(alert: WeatherAlert) {
        val updatedAlerts = _alerts.value.toMutableList().apply { remove(alert) }
        _alerts.emit(updatedAlerts)


    }

    override fun getAllAlerts(): Flow<List<WeatherAlert>> = _alerts

    override suspend fun insertCashedData(weatherResponse: WeatherResponse) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCashedData() {
        TODO("Not yet implemented")
    }

    override fun getCashedData(): Flow<WeatherResponse>? {
        TODO("Not yet implemented")
    }

}
