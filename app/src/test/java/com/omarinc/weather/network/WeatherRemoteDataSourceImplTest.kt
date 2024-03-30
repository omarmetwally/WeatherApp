package com.omarinc.weather.network
/*

import com.omarinc.weather.model.CityInfo
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.WeatherResponse
import com.omarinc.weather.network.WeatherApiService
import com.omarinc.weather.network.WeatherRemoteDataSourceImpl
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class WeatherRemoteDataSourceImplTest {

    @Test
    fun getWeatherResponse_ReturnsWeatherResponse_OnSuccessfulFetch() = runBlocking {
        val mockService = mockk<WeatherApiService>()

        val dummyCoordinates = Coordinates(1.0, 2.0)
        val dummyCityInfo = CityInfo(
            id = 1,
            name = "Dummy City",
            coord = dummyCoordinates,
            country = "Dummy Country",
            population = 100000,
            timezone = 12345,
            sunrise = 1234567890L,
            sunset = 123456790L
        )
        val dummyWeatherResponse = WeatherResponse(
            cod = "200",
            message = 0,
            cnt = 0,
            list = emptyList(),
            city = dummyCityInfo
        )
        val successfulResponse = Response.success(dummyWeatherResponse)

        // Setup the mock to return the expected response
        coEvery {
            mockService.getForecast(
                latitude = dummyCoordinates.lat,
                longitude = dummyCoordinates.lon,
                language = any(),
                units = any()
            )
        } returns successfulResponse

      val dataSource = WeatherRemoteDataSourceImpl(mockService) // lesa me7tag a3ml DI ll remote el awl

        val result = dataSource.getWeatherResponse(dummyCoordinates, "en", "metric").first()

        assertEquals("200", result.body()?.cod)
        assertEquals("Dummy City", result.body()?.city?.name)
    }
}
*/
