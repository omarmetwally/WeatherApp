package com.omarinc.weather.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.omarinc.weather.model.FavoriteCity
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.CoreMatchers.hasItems
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@SmallTest
class FavouriteDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: WeatherDB
    private lateinit var dao: FavouriteDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDB::class.java
        ).allowMainThreadQueries().build()
        dao = database.getDao()
    }
    @After
    fun closeDb() {
        database.close()
    }


    @Test
    fun insertFavoriteCity_savesData() = runBlockingTest {
        val city = FavoriteCity(1, "Country", 0.0, 0.0)
        dao.insertFavoriteCity(city)

        val allCities = dao.getAllFavouriteCites().first()
        assertThat(allCities, hasItem(city))
    }

    @Test
    fun deleteFavoriteCity_removesData() = runBlockingTest {
        val city = FavoriteCity(1, "Country", 0.0, 0.0)
        dao.insertFavoriteCity(city)
        dao.deleteFavoriteCity(city)

        val allCities = dao.getAllFavouriteCites().first()
        assertThat(allCities, not(hasItem(city)))
    }


    @Test
    fun getAllFavouriteCites_emptyListWhenNoCitiesAdded() = runBlockingTest {
        val allCities = dao.getAllFavouriteCites().first()
        assertThat(allCities, `is`(empty()))
    }


    @Test
    fun getAllFavouriteCites_containsInsertedCities() = runBlockingTest {
        val city1 = FavoriteCity(1, "October", 0.0, 0.0)
        val city2 = FavoriteCity(2, "iTi", 0.0, 0.0)
        dao.insertFavoriteCity(city1)
        dao.insertFavoriteCity(city2)

        val allCities = dao.getAllFavouriteCites().first()
        assertThat(allCities, hasItems(city1, city2))
    }

    @Test
    fun getAllFavouriteCites_reflectsChangesAfterDeletion() = runBlockingTest {
        val city1 = FavoriteCity(1, "Country1", 0.0, 0.0)
        val city2 = FavoriteCity(2, "Country2", 0.0, 0.0)
        dao.insertFavoriteCity(city1)
        dao.insertFavoriteCity(city2)
        dao.deleteFavoriteCity(city1)

        val allCities = dao.getAllFavouriteCites().first()
        assertThat(allCities, not(hasItem(city1)))
        assertThat(allCities, hasItem(city2))
    }


    // bt2akd in flow emits a5er data b3d Update
    @Test
    fun getAllFavouriteCites_emitsLatestData() = runBlockingTest {
        val city = FavoriteCity(1, "october", 0.0, 0.0)
        dao.insertFavoriteCity(city)

        val job = launch {
            dao.getAllFavouriteCites().collect { allCities ->

                if (allCities.any { it.cityName == "October2 Updated" }) {
                    val updatedCity = allCities.first { it.cityName == "October2 Updated" }
                    assertThat(updatedCity.cityName, `is`("October2 Updated"))
                    cancel()
                }
            }
        }

        val updatedCity = city.copy(cityName = "October2 Updated")
        dao.insertFavoriteCity(updatedCity)

        job.join()
    }

}