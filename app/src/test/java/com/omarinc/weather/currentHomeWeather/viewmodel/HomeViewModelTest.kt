package com.omarinc.weather.currentHomeWeather.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.omarinc.weather.Mocks.FakeWeatherRepository
import com.omarinc.weather.model.*
import com.omarinc.weather.network.ApiState
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.*
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import retrofit2.Response
@ExperimentalCoroutinesApi
//@RunWith(RobolectricTestRunner::class)
@RunWith(AndroidJUnit4::class)

class HomeViewModelTest {

    @get:Rule
    var instantExecutorRule: TestRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeViewModel
    private lateinit var fakeRepository: FakeWeatherRepository
    private val context = mockk<Context>(relaxed = true)

    @ExperimentalCoroutinesApi
    val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeWeatherRepository()
        viewModel = HomeViewModel(fakeRepository, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getCurrentWeather_success() = runTest {
        val dummyWeatherResponse = createDummyWeatherResponse()
        fakeRepository.response = Response.success(dummyWeatherResponse)

        val dummyCoordinates = Coordinates(0.0, 0.0)
        viewModel.getCurrentWeather(dummyCoordinates, "en", "metric")

        var collectedState: ApiState? = null

        withTimeoutOrNull(timeMillis = 1000) {
            viewModel.weather.collect { state ->
                collectedState = state
                if (state is ApiState.Success) return@collect
            }
        }


        val weatherState = viewModel.weather.first()
//        Assert.assertTrue("Expected ApiState.Success but was $weatherState", weatherState is ApiState.Success)

        Assert.assertEquals(dummyWeatherResponse, (weatherState as ApiState.Success).weatherResponse)
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
                    weather = listOf(WeatherCondition(800, "Clear", "clear sky", "01d")),
                    clouds = Clouds(all = 0),
                    wind = Wind(speed = 1.5, deg = 350, gust = 1.5),
                    visibility = 10000,
                    pop = 0.0,
                    sys = Sys(pod = "d"),
                    dt_txt = "2020-09-13 00:00:00"
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
