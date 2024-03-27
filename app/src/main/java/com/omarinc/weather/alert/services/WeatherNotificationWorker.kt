package com.omarinc.weather.alert.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.omarinc.weather.R
import com.omarinc.weather.db.WeatherLocalDataSourceImpl
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.WeatherRepositoryImpl
import com.omarinc.weather.network.WeatherRemoteDataSourceImpl
import com.omarinc.weather.sharedpreferences.SharedPreferencesImpl
import com.omarinc.weather.utilities.Constants
import com.omarinc.weather.utilities.Helpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Locale

class WeatherNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private  val TAG = "WeatherNotificationWork"

    companion object {
        const val CHANNEL_ID = "weather_alert_channel"
        const val NOTIFICATION_ID = 10
    }

    /*override suspend fun doWork(): Result {
        // Set up notification channel
        setupNotificationChannel()

        // Here you will fetch the weather data and prepare the notification content.
        // For demonstration, let's use a placeholder for weather condition
        val weatherCondition = "Sunny"

        // Now, create and show the notification
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Weather Alert")
            .setContentText("Weather Condition: $weatherCondition")
            .setSmallIcon(R.drawable.ic_alert_notification)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)

        return Result.success()
    }*/
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {


//        val contextWithCorrectLocale = Helpers.updateLocale(applicationContext)
        val prefs = SharedPreferencesImpl.getInstance(applicationContext)
        val lang = prefs.readStringFromSharedPreferences(Constants.KEY_LANGUAGE)
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = applicationContext.resources.configuration
        config.setLocale(locale)

        val contextWithCorrectLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            applicationContext.createConfigurationContext(config)
        } else {
            applicationContext
        }


        val repository = WeatherRepositoryImpl.getInstance(
            WeatherRemoteDataSourceImpl.getInstance(contextWithCorrectLocale),
            WeatherLocalDataSourceImpl.getInstance(contextWithCorrectLocale),
            SharedPreferencesImpl.getInstance(contextWithCorrectLocale)
        )



        val latitude = inputData.getDouble(Constants.LATITUDE_KEY, 0.0)
        val longitude = inputData.getDouble(Constants.LONGITUDE_KEY, 0.0)
        val location = inputData.getString(Constants.LOCATION_NAME_KEY)
        try {
            val response = repository.getWeatherResponse(Coordinates(latitude,longitude),
                SharedPreferencesImpl.getInstance(context = contextWithCorrectLocale).readStringFromSharedPreferences(Constants.KEY_LANGUAGE),
                "metric").first()
            if (response.isSuccessful) {
                response.body()?.let { weatherResponse ->
                    val weatherCondition = weatherResponse.list.first().weather.first().description

                    Log.i(TAG, "doWork: $weatherCondition")
                    Log.i(TAG, "doWork: ${weatherResponse.city.name}")

                    val notificationManager = contextWithCorrectLocale.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    val notification = NotificationCompat.Builder(contextWithCorrectLocale, CHANNEL_ID)
                        .setContentTitle(contextWithCorrectLocale.getString(R.string.weather_alert))
                        .setContentText("${contextWithCorrectLocale.getString(R.string.weather_Condition)} " +
                                "${location}: " +
                                "$weatherCondition")
                        .setSmallIcon(R.drawable.ic_alert_notification)
                        .build()

                    notificationManager.notify(NOTIFICATION_ID, notification)

                    Result.success()
                } ?: Result.failure()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }



    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationContext.getString(R.string.channel_name)
            val descriptionText = applicationContext.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            Log.i(TAG, "setupNotificationChannel: ")
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }



}
