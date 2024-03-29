package com.omarinc.weather.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.omarinc.weather.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class WeatherDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: WeatherDB
    private lateinit var weatherDao: WeatherDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDB::class.java
        ).allowMainThreadQueries().build()
        weatherDao = database.getWeatherDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertCashedData_savesData() = runBlockingTest {
        val weatherData = WeatherResponse(
            cod = "200",
            message = 0,
            cnt = 40,
            list = listOf(ForecastEntry(
                dt = 1618317040,
                main = MainWeatherData(
                    temp = 284.92,
                    feels_like = 281.38,
                    temp_min = 284.15,
                    temp_max = 284.92,
                    pressure = 1019,
                    sea_level = 1019,
                    grnd_level = 915,
                    humidity = 62,
                    temp_kf = 0.53
                ),
                weather = listOf(WeatherCondition(
                    id = 500,
                    main = "Rain",
                    description = "light rain",
                    icon = "10d"
                )),
                clouds = Clouds(all = 100),
                wind = Wind(speed = 6.17, deg = 186, gust = 11.83),
                visibility = 10000,
                pop = 0.2,
                sys = Sys(pod = "d"),
                dt_txt = "2024-03-25 09:00:00"
            )),
            city = CityInfo(
                id = 2643743,
                name = "London",
                coord = Coordinates(lat = 51.5074, lon = -0.1278),
                country = "GB",
                population = 1000000,
                timezone = 0,
                sunrise = 1618282134,
                sunset = 1618333901
            )
        )
        weatherDao.insertCashedData(weatherData)

        val cashedData = weatherDao.getCashedData().first()
        assertThat(cashedData, notNullValue())
        assertThat(cashedData.cod, `is`(weatherData.cod))
    }

    @Test
    fun deleteCashedData_removesData() = runBlockingTest {
        val weatherData = WeatherResponse(
            cod = "200",
            message = 0,
            cnt = 40,
            list = emptyList(),
            city = CityInfo(id = 0, name = "October", coord = Coordinates(0.0, 0.0), country = "Egy", population = 1, timezone = 1, sunrise = 0, sunset = 0)
        )
        weatherDao.insertCashedData(weatherData)
        weatherDao.deleteCashedData()

        val cashedData = weatherDao.getCashedData().firstOrNull()
        assertThat(cashedData, `is`(nullValue()))
    }
}
