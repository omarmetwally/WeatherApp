package com.omarinc.weather.alert.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.omarinc.weather.Mocks.FakeWeatherRepository
import com.omarinc.weather.model.WeatherAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith



@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class AlertViewModelTest{
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

//    @get:Rule
//    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: AlertViewModel
    private lateinit var fakeRepository: FakeWeatherRepository

    @ExperimentalCoroutinesApi
    private val testDispatcher = TestCoroutineDispatcher()


    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeWeatherRepository()
        viewModel = AlertViewModel(fakeRepository, ApplicationProvider.getApplicationContext())
    }

    @ExperimentalCoroutinesApi

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }



    @Test
    fun insertAlert_updatesStateFlowCorrectly() = runTest {
        val alert = WeatherAlert(1, "Test Location", 1.0, 1.0, 12345678L)

        viewModel.insertAlert(alert)


        val result = viewModel.alert.first()

        assertThat( result.contains(alert), `is`(true))

    }

    @Test
    fun getAllAlerts_updatesStateFlowCorrectly() = runTest {
        val alert1 = WeatherAlert(1, "Location 1", 1.0, 1.0, 12345678L)
        viewModel.insertAlert(alert1)

        val result = viewModel.alert.first()
        assertThat(result.size, `is`(1))

        assertThat(result, `is`(listOf(alert1)))
    }



    @Test
    fun deleteAlert_updatesStateFlowCorrectly() = runTest {
        // Insert an alert to be deleted.
        val alert = WeatherAlert(1, "Test Location", 1.0, 1.0, 12345678L)
        viewModel.insertAlert(alert)
        viewModel.deleteAlert(alert)


        var result = viewModel.alert.first()

        assertThat(result.contains(alert), `is`(false))
    }







//
//    @Test
//    fun deleteAlert_updatesStateFlowCorrectly() = runTest {
//        val alert = WeatherAlert(1, "Test Location", 1.0, 1.0, 12345678L)
//        viewModel.insertAlert(alert)
//
//        viewModel.deleteFavorite(alert)
//        viewModel.getAllAlerts()
//        advanceUntilIdle()
//
//        val currentState = viewModel.alert.first()
//        assertThat(currentState, instanceOf(DataBaseState.Success::class.java))
//        val successState = currentState as DataBaseState.Success
//        assertFalse(successState.data.contains(alert))
//    }
//
//    @Test
//    fun getAllAlerts_retrievesCorrectData() = runTest {
//        val alert1 = WeatherAlert(1, "Test Location 1", 1.0, 1.0, 12345678L)
//        val alert2 = WeatherAlert(2, "Test Location 2", 2.0, 2.0, 12345679L)
//        viewModel.insertAlert(alert1)
//        viewModel.insertAlert(alert2)
//
//        viewModel.getAllAlerts()
//        advanceUntilIdle()
//
//        val currentState = viewModel.alert.first()
//        assertThat(currentState, instanceOf(DataBaseState.Success::class.java))
//        val successState = currentState as DataBaseState.Success
//        assertTrue(successState.data.size == 2)
//        assertTrue(successState.data.containsAll(listOf(alert1, alert2)))
//    }
//
//
//
//    suspend fun awaitAlertStateSuccess(timeout: Long = 5000): Boolean {
//        return withTimeoutOrNull(timeout) {
//            viewModel.alert.collect { state ->
//                if (state is DataBaseState.Success) return@collect
//            }
//            false
//        } ?: false
//    }

}
