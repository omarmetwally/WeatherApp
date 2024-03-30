package com.omarinc.weather.model


import com.omarinc.weather.Mocks.FakeSharedPreferences
import com.omarinc.weather.Mocks.FakeWeatherLocalDataSource
import com.omarinc.weather.Mocks.FakeWeatherRemoteDataSource
import com.omarinc.weather.model.WeatherRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Response


@ExperimentalCoroutinesApi
class WeatherRepositoryImplTest {
    private lateinit var weatherRepository: WeatherRepositoryImpl
    private lateinit var fakeWeatherRemoteDataSource: FakeWeatherRemoteDataSource
    private lateinit var fakeWeatherLocalDataSource: FakeWeatherLocalDataSource
    private lateinit var fakeSharedPreferences: FakeSharedPreferences

    @Before
    fun setup() {
        fakeWeatherRemoteDataSource = FakeWeatherRemoteDataSource()
        fakeWeatherLocalDataSource = FakeWeatherLocalDataSource()
        fakeSharedPreferences = FakeSharedPreferences()
        weatherRepository = WeatherRepositoryImpl(fakeWeatherRemoteDataSource, fakeWeatherLocalDataSource, fakeSharedPreferences)
    }

    @Test
    fun getWeatherResponse_returnsExpectedData() = runTest {
        val expectedResponse = createDummyWeatherResponse()

        fakeWeatherRemoteDataSource.response = Response.success(expectedResponse)


        val result = weatherRepository.getWeatherResponse(Coordinates(0.0, 0.0), "en", "metric").first().body()
        assertEquals(expectedResponse, result)

    }


    @Test
    fun insertFavorite_savesDataCorrectly() = runTest {
        val dummyCity = FavoriteCity(id = 1, cityName = "October", latitude = 1.0, longitude = 2.0)

        weatherRepository.insertFavorite(dummyCity)

        val favorites = weatherRepository.getAllFavorites().first()

        assertThat(favorites, hasItem(dummyCity))
    }
    @Test
    fun deleteFavorite_removesDataCorrectly() = runTest {
        val dummyCity = FavoriteCity(id = 1, cityName = "October", latitude = 1.0, longitude = 2.0);
        weatherRepository.insertFavorite(dummyCity);

        weatherRepository.deleteFavorite(dummyCity);
        val favorites = weatherRepository.getAllFavorites().first();

        assertThat(favorites, not(hasItem(dummyCity)));
    }

    @Test
    fun insertAlert_savesDataCorrectly() = runTest {
        val alert = WeatherAlert(1, "Test Alert", 1.0, 2.0, 1234567890L);

        weatherRepository.insertAlert(alert);
        val alerts = weatherRepository.getAllAlerts().first();

        assertThat(alerts, hasItem(alert));
    }
    @Test
    fun deleteAlert_removesDataCorrectly() = runTest {
        val alert = WeatherAlert(1, "Test Alert", 1.0, 2.0, 1234567890L);

        weatherRepository.insertAlert(alert);
        weatherRepository.deleteAlert(alert);
        val alerts = weatherRepository.getAllAlerts().first();

        assertThat(alerts, not(hasItem(alert)));
    }

    @Test
    fun insertCashedData_savesDataCorrectly() = runTest {
        val weatherResponse = createDummyWeatherResponse();

        weatherRepository.insertCashedData(weatherResponse);
        val cashedData = weatherRepository.getCashedData()?.first();

        assertEquals(weatherResponse, cashedData);
    }

    @Test
    fun deleteCashedData_clearsDataCorrectly() = runTest {
        val weatherResponse = createDummyWeatherResponse();

        weatherRepository.insertCashedData(weatherResponse);
        weatherRepository.deleteCashedData();
        val cashedData = weatherRepository.getCashedData()?.first();

        assertNull(cashedData);
    }

    @Test
    fun writeAndReadStringToSharedPreferences_savesAndRetrievesStringCorrectly() = runTest {
        val key = "test_key"
        val value = "test_value"

        weatherRepository.writeStringToSharedPreferences(key, value)
        val retrievedValue = weatherRepository.readStringFromSharedPreferences(key)

        assertEquals(value, retrievedValue)
    }

    @Test
    fun writeAndReadCoordinatesToSharedPreferences_savesAndRetrievesCoordinatesCorrectly() = runTest {
        val keyLat = "latitude_key"
        val keyLon = "longitude_key"
        val valueLat = 1.234
        val valueLon = 5.678

        weatherRepository.writeCoordinatesToSharedPreferences(keyLat, valueLat)
        weatherRepository.writeCoordinatesToSharedPreferences(keyLon, valueLon)
        val retrievedLat = weatherRepository.readSCoordinatesFromSharedPreferences(keyLat)
        val retrievedLon = weatherRepository.readSCoordinatesFromSharedPreferences(keyLon)

        assertEquals(valueLat, retrievedLat, 0.001)
        assertEquals(valueLon, retrievedLon, 0.001)
    }





    private fun createDummyWeatherResponse(): WeatherResponse {
        return WeatherResponse(
            cod = "200",
            message = 0,
            cnt = 1,
            list = listOf(
                ForecastEntry(
                    dt = 1600000000L,
                    main = MainWeatherData(
                        temp = 20.0,
                        feels_like = 19.0,
                        temp_min = 18.0,
                        temp_max = 22.0,
                        pressure = 1012,
                        sea_level = 1012,
                        grnd_level = 1012,
                        humidity = 80,
                        temp_kf = 0.0
                    ),
                    weather = listOf(
                        WeatherCondition(
                            id = 800,
                            main = "Clear",
                            description = "clear sky",
                            icon = "01d"
                        )
                    ),
                    clouds = Clouds(all = 0),
                    wind = Wind(speed = 1.5, deg = 350, gust = 1.5),
                    visibility = 10000,
                    pop = 0.0,
                    sys = Sys(pod = "d"),
                    dt_txt = "2024-03-20 00:00:00"
                )
            ),
            city = CityInfo(
                id = 1,
                name = "Dummy City",
                coord = Coordinates(lat = 1.0, lon = 2.0),
                country = "Dummy Country",
                population = 100000,
                timezone = 7200,
                sunrise = 1600000000L,
                sunset = 1600040000L
            )
        )
    }

}
