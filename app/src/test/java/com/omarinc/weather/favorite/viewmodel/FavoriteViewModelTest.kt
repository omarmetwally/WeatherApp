package com.omarinc.weather.favorite.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.omarinc.weather.Mocks.FakeWeatherRepository
import com.omarinc.weather.model.FavoriteCity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class FavoriteViewModelTest{
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel:FavoriteViewModel
    private lateinit var fakeRepository: FakeWeatherRepository

    @ExperimentalCoroutinesApi
    private val testDispatcher = TestCoroutineDispatcher()

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeWeatherRepository()
        viewModel = FavoriteViewModel(fakeRepository, ApplicationProvider.getApplicationContext())
    }

    @ExperimentalCoroutinesApi

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }


    @Test
    fun insertFavorite_updatesStateFlowCorrectly()= runTest {
        val favorite=FavoriteCity(1,"October",0.0,0.0)
        viewModel.insertFavorite(favorite)

        val result=viewModel.favorite.first()
        assertThat(result.contains(favorite),`is`(true))
    }


    @Test
    fun getAllFavorites_updatesStateFlowCorrectly()= runTest {
        val favorite=FavoriteCity(1,"October",0.0,0.0)
        viewModel.insertFavorite(favorite)

        val result=viewModel.favorite.first()

        assertThat(result.size, Is.`is`(1))

        assertThat(result, Is.`is`(listOf(favorite)))
    }


}