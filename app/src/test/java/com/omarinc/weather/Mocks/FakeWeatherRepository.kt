package com.omarinc.weather.Mocks

import android.util.Log
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.WeatherAlert
import com.omarinc.weather.model.WeatherRepository
import com.omarinc.weather.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
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
        _favorites.emit(updatedFavorite)
    }

    override suspend fun getAllFavorites(): Flow<List<FavoriteCity>> = _favorites


    private val preferences = mutableMapOf<String, Any>()

    override fun writeStringToSharedPreferences(key: String, value: String) {
        preferences[key] = value
    }

    override fun readStringFromSharedPreferences(key: String): String {
        return preferences[key] as? String ?: ""
    }

    override fun writeCoordinatesToSharedPreferences(key: String, value: Double) {
        preferences[key] = value
    }

    override fun readSCoordinatesFromSharedPreferences(key: String): Double {
        return preferences[key] as? Double ?: 0.0
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



    private val _cachedData = MutableStateFlow<WeatherResponse?>(null)


    override suspend fun insertCashedData(weatherResponse: WeatherResponse) {
//        val updatedCachedData = _cachedData.value.toMutableList().apply { add(weatherResponse) }
//        _cachedData.emit(updatedCachedData)
        _cachedData.value= weatherResponse

    }

    override suspend fun deleteCashedData() {
        _cachedData.value= null
    }

    override fun getCashedData(): Flow<WeatherResponse> = _cachedData.filterNotNull()
    override suspend fun toggleAlertNotificationState(alertId: Int, isEnabled: Boolean) {
        TODO("Not yet implemented")
    }

}
