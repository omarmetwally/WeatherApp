package com.omarinc.weather.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.omarinc.weather.model.CityInfo
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.WeatherAlert
import com.omarinc.weather.model.WeatherResponse
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class WeatherLocalDataSourceImplTest
{
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: WeatherDB
    private lateinit var localDataSource: WeatherLocalDataSourceImpl
    private lateinit var favoriteDao: FavouriteDao
    private lateinit var alertDao: WeatherAlertDao
    private lateinit var weatherDao: WeatherDao

    @Before
    fun init() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDB::class.java
        ).allowMainThreadQueries().build()

        favoriteDao = database.getDao()
        alertDao = database.getAlertDao()
        weatherDao = database.getWeatherDao()
        localDataSource = WeatherLocalDataSourceImpl.getInstance(ApplicationProvider.getApplicationContext())
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertFavoriteCity_and_getFavoriteCities() = runBlocking {
        val city = FavoriteCity(1, "TestCity", 1.0, 2.0)
        localDataSource.insert(city)
        val cities = localDataSource.getFavoriteCities().first()
        assertTrue(cities.contains(city))
    }

    @Test
    fun deleteFavoriteCity_and_getFavoriteCities() = runBlocking {
        val city = FavoriteCity(1, "TestCity", 1.0, 2.0)
        localDataSource.insert(city)
        localDataSource.delete(city)
        val cities = localDataSource.getFavoriteCities().first()
        assertFalse(cities.contains(city))
    }

    @Test
    fun insertAlert_and_getAllAlerts() = runTest {
        val alert = WeatherAlert(1, "Test Alert", 0.0, 0.0,0)
        localDataSource.insertAlert(alert)
        val alerts = localDataSource.getAllAlerts().first()
        assertTrue(alerts.contains(alert))
    }

    @Test
    fun deleteAlert_and_getAllAlerts() = runBlocking {
        val alert = WeatherAlert(7, "Test Alert", 0.0, 0.0,0)
        localDataSource.insertAlert(alert)
        localDataSource.deleteAlert(alert)
        val alerts = localDataSource.getAllAlerts().first()
        assertFalse(alerts.contains(alert))
    }

    @Test
    fun insertCashedData_and_getCashedData() = runBlocking {
        val weatherResponse = WeatherResponse("200", 1, 1, listOf(), CityInfo(1, "Test City", Coordinates(0.0, 0.0), "Country", 0, 0, 0, 0))
        localDataSource.insertCashedData(weatherResponse)
        val cachedData = localDataSource.getCashedData().first()
        assertThat(cachedData, `is`(weatherResponse))
    }

    @Test
    fun deleteCashedData_and_getCashedData() = runBlocking {
        val weatherResponse = WeatherResponse("200", 1, 1, listOf(), CityInfo(1, "Test City", Coordinates(0.0, 0.0), "Country", 0, 0, 0, 0))
        localDataSource.insertCashedData(weatherResponse)
        localDataSource.deleteCashedData()
        val cachedData = localDataSource.getCashedData().first()
        assertThat(cachedData, `is`(nullValue()))
    }
}