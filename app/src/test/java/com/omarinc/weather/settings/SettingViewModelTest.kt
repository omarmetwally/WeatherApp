package com.omarinc.weather.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.omarinc.weather.Mocks.FakeWeatherRepository
import com.omarinc.weather.favorite.viewmodel.FavoriteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class SettingViewModelTest
{
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SettingViewModel
    private lateinit var fakeRepository: FakeWeatherRepository

    @ExperimentalCoroutinesApi
    private val testDispatcher = TestCoroutineDispatcher()

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeWeatherRepository()
        viewModel = SettingViewModel(fakeRepository, ApplicationProvider.getApplicationContext())
    }

    @ExperimentalCoroutinesApi

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun writeAndReadStringToSharedPreferences_correctlyStoresData() = runTest {
        val key = "test_string_key"
        val value = "test_value"
        viewModel.writeStringToSharedPreferences(key, value)

        val retrievedValue = viewModel.readStringFromSharedPreferences(key)
        assertThat(retrievedValue, `is`(value))
    }

    @Test
    fun writeAndReadCoordinatesToSharedPreferences_correctlyStoresData() = runTest {
        val key = "test_coordinates_key"
        val value = 1.23
        viewModel.writeCoordinatesToSharedPreferences(key, value)

        val retrievedValue = viewModel.readCoordinatesFromSharedPreferences(key)
        assertThat(retrievedValue, `is`(value))
    }



}