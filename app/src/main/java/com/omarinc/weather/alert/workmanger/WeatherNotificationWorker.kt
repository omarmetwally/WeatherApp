package com.omarinc.weather.alert.workmanger

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.omarinc.weather.R
import com.omarinc.weather.db.WeatherLocalDataSourceImpl
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.WeatherRepositoryImpl
import com.omarinc.weather.network.WeatherRemoteDataSourceImpl
import com.omarinc.weather.sharedpreferences.SharedPreferencesImpl
import com.omarinc.weather.utilities.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

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
        val repository = WeatherRepositoryImpl.getInstance(
            WeatherRemoteDataSourceImpl.getInstance(applicationContext),
            WeatherLocalDataSourceImpl.getInstance(applicationContext),
            SharedPreferencesImpl.getInstance(applicationContext)
        )

        val latitude = inputData.getDouble(Constants.LATITUDE_KEY, 0.0)
        val longitude = inputData.getDouble(Constants.LONGITUDE_KEY, 0.0)
        val location = inputData.getString(Constants.LOCATION_NAME_KEY)
        try {
            val response = repository.getWeatherResponse(Coordinates(latitude,longitude), "en", "metric").first()
            if (response.isSuccessful) {
                response.body()?.let { weatherResponse ->
                    val weatherCondition = weatherResponse.list.first().weather.first().main

                    Log.i(TAG, "doWork: $weatherCondition")
                    Log.i(TAG, "doWork: ${weatherResponse.city.name}")

//                    setupNotificationChannel()

                    val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                    val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                        .setContentTitle("Weather Alert")
                        .setContentText("Weather Condition in ${location}: $weatherCondition")
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
