package com.omarinc.weather.Mocks

import com.omarinc.weather.db.WeatherLocalDataSource
import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.WeatherAlert
import com.omarinc.weather.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeWeatherLocalDataSource : WeatherLocalDataSource {
    var favorites: MutableList<FavoriteCity> = mutableListOf()
    var alerts: MutableList<WeatherAlert> = mutableListOf()
    var cashedData: WeatherResponse? = null

    override suspend fun insert(city: FavoriteCity) {
        favorites.add(city)
    }

    override suspend fun delete(city: FavoriteCity) {
        favorites.remove(city)
    }

    override suspend fun getFavoriteCities(): Flow<List<FavoriteCity>> = flowOf(favorites)

    override suspend fun insertAlert(alert: WeatherAlert) {
        alerts.add(alert)
    }

    override suspend fun deleteAlert(alert: WeatherAlert) {
        alerts.remove(alert)
    }

    override fun getAllAlerts(): Flow<List<WeatherAlert>> = flowOf(alerts)

    override suspend fun insertCashedData(weatherResponse: WeatherResponse) {
        cashedData = weatherResponse
    }

    override suspend fun deleteCashedData() {
        cashedData = null
    }

    override fun getCashedData(): Flow<WeatherResponse>? = cashedData?.let { flowOf(it) }
    override suspend fun toggleAlertNotificationState(alertId: Int, isEnabled: Boolean) {
        TODO("Not yet implemented")
    }
}
