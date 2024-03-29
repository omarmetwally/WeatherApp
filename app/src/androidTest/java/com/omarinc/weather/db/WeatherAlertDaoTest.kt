package com.omarinc.weather.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.omarinc.weather.model.WeatherAlert
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.hasItem
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
class WeatherAlertDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: WeatherDB
    private lateinit var weatherAlertDao: WeatherAlertDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDB::class.java
        ).allowMainThreadQueries().build()
        weatherAlertDao = database.getAlertDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAlert_savesData() = runBlockingTest {
        val weatherAlert =  WeatherAlert(1, "October",0.0,0.0,123456)
        weatherAlertDao.insertAlert(weatherAlert)

        val allAlerts = weatherAlertDao.getAllAlerts().first()
        assertThat(allAlerts, hasItem(weatherAlert))
    }

    @Test
    fun deleteAlert_removesData() = runBlockingTest {
        val weatherAlert = WeatherAlert(1, "October",0.0,0.0,123456)
        weatherAlertDao.insertAlert(weatherAlert)
        weatherAlertDao.deleteAlert(weatherAlert)

        val allAlerts = weatherAlertDao.getAllAlerts().first()
        assertThat(allAlerts, not(hasItem(weatherAlert)))
    }

    @Test
    fun getAllAlerts_emptyListWhenNoAlertsAdded() = runBlockingTest {
        val allAlerts = weatherAlertDao.getAllAlerts().first()
        assertThat(allAlerts, `is`(empty()))
    }

    @Test
    fun getAllAlerts_containsInsertedAlerts() = runBlockingTest {
        val alert1 =  WeatherAlert(1, "October",0.0,0.0,123456)
        val alert2 =  WeatherAlert(2, "iTi ",0.0,0.0,123456)
        weatherAlertDao.insertAlert(alert1)
        weatherAlertDao.insertAlert(alert2)

        val allAlerts = weatherAlertDao.getAllAlerts().first()
        assertThat(allAlerts, hasItem(alert1))
        assertThat(allAlerts, hasItem(alert2))
    }

    @Test
    fun getAllAlerts_reflectsChangesAfterDeletion() = runBlockingTest {
        val alert1 =  WeatherAlert(1, "October",0.0,0.0,123456)
        val alert2 =  WeatherAlert(2, "iTi",0.0,0.0,123456)
        weatherAlertDao.insertAlert(alert1)
        weatherAlertDao.insertAlert(alert2)
        weatherAlertDao.deleteAlert(alert1)

        val allAlerts = weatherAlertDao.getAllAlerts().first()
        assertThat(allAlerts, not(hasItem(alert1)))
        assertThat(allAlerts, hasItem(alert2))
    }
}
