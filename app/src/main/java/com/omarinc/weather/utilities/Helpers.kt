package com.omarinc.weather.utilities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.airbnb.lottie.LottieAnimationView
import com.omarinc.weather.R
import com.omarinc.weather.alert.services.AlarmReceiver
import com.omarinc.weather.alert.services.WeatherNotificationWorker
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.WeatherResponse
import com.omarinc.weather.sharedpreferences.SharedPreferencesImpl
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

object Helpers {

    private  val TAG = "Helpers"
    fun setAppLanguage(activity: Activity, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources: Resources = activity.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        val layoutDirection = if (languageCode == "ar") {
            View.LAYOUT_DIRECTION_RTL
        } else {
            View.LAYOUT_DIRECTION_LTR
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLayoutDirection(locale)
            activity.window.decorView.layoutDirection = layoutDirection
        }

        resources.updateConfiguration(config, resources.displayMetrics)
//        activity.finish()
        activity.recreate()
//        reloadApp()
    }

//    private fun reloadApp() {
//        val intent = requireActivity().packageManager.getLaunchIntentForPackage(
//            requireActivity().packageName
//        )
//        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//        requireActivity().finish()
//        if (intent != null) {
//            startActivity(intent)
//        }
//    }


    @SuppressLint("SetTextI18n")
    fun setLocationNameByGeoCoder(coordinates: Coordinates, context: Context): String {
      var  geocoder = Geocoder(context, Locale.getDefault())

        var addressText:String=""
        var addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocation(coordinates.lat, coordinates.lon, 1)
            if (addresses != null) {
                addressText = addresses.firstOrNull()?.subAdminArea+" , "+addresses.firstOrNull()?.countryName ?: context.getString(
                    R.string.address_not_found
                )
            }
        } catch (e: IOException) {
            addressText = "Unable to get address"
        }



        return addressText
    }


    fun updateLocale(context: Context): Context {
        val prefs = SharedPreferencesImpl.getInstance(context)
        val lang = prefs.readStringFromSharedPreferences(Constants.KEY_LANGUAGE) ?: Locale.getDefault().language
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun celsiusToFahrenheit(celsius: Int): Int {


        return (celsius * 9/5) + 32
    }

    fun decideUnit(unit:String,context: Context):String{
        when(unit){
            Constants.VALUE_CELSIUS -> return context.getString(R.string.c)
            Constants.VALUE_FAHRENHEIT -> return context.getString(R.string.f)
            Constants.VALUE_KELVIN -> return context.getString(R.string.k)
            else -> return context.getString(R.string.c)

        }
       return ""
    }
    fun celsiusToKelvin(celsius: Int): Int {
        return celsius + 273
    }

    fun meterPerSecondToMilePerHour(meterPerSecond: Double): Double {
        return meterPerSecond * 2.23694
    }



     @RequiresApi(Build.VERSION_CODES.O)
     fun disableNotificationChannel(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val notificationManager: NotificationManager =
//                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//            val channel = notificationManager.getNotificationChannel(WeatherNotificationWorker.CHANNEL_ID)
//            if (channel != null) {
//                channel.importance = NotificationManager.IMPORTANCE_NONE
//                notificationManager.createNotificationChannel(channel)
//                Log.i(TAG, "Notification Channel Disabled")
//            }
//        }

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             val notificationManager: NotificationManager =
                 context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
             notificationManager.deleteNotificationChannel(WeatherNotificationWorker.CHANNEL_ID)

//             setupNotificationChannel(context, NotificationManager.IMPORTANCE_HIGH)
         }
    }

     fun setupNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            Log.i(TAG, "setupNotificationChannel: ")
            val channel = NotificationChannel(WeatherNotificationWorker.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (context is Activity) {
                    ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.POST_NOTIFICATIONS),101)
                }
            }

        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
//            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                Uri.parse("package:com.omarinc.weather"))
//            startActivityForResult(intent, 101)
//        }

    }


    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    fun registerAlarm(context: Context, alertId: Int, lat: Double, lng: Double, locationName: String, alertTimeMillis: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(Constants.LATITUDE_KEY, lat)
            putExtra(Constants.LONGITUDE_KEY, lng)
            putExtra(Constants.LOCATION_NAME_KEY, locationName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alertId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alertTimeMillis,
            pendingIntent
        )
    }

    fun registerNotification(context: Context, alertId: Int, lat: Double, lng: Double, locationName: String, timeStamp: Long) {
        Log.i(TAG, "registerNotification: $alertId $locationName")
        val alertDelay = timeStamp - System.currentTimeMillis()
        if (alertDelay > 0) {
            val data = Data.Builder()
                .putDouble(Constants.LATITUDE_KEY, lat)
                .putDouble(Constants.LONGITUDE_KEY, lng)
                .putString(Constants.LOCATION_NAME_KEY, locationName)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<WeatherNotificationWorker>()
                .setInitialDelay(alertDelay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork("Notification_$alertId", ExistingWorkPolicy.REPLACE, workRequest)
        }
    }


    fun setLottieAnimation(id: String, animationView: LottieAnimationView) {
        val animationResId = when (id) {
            "01d" -> R.raw.clear_sky_anim
            "02d" -> R.raw.few_clouds
            "03d" -> R.raw.few_clouds
            "04d" -> R.raw.few_clouds
            "09d" -> R.raw.rain_anim
            "10d" -> R.raw.rain_anim
            "11d" -> R.raw.thunderstorm
            "13d" -> R.raw.snow_anim
            "50d" -> R.raw.mist
            "01n" -> R.raw.clear_sky_anim_night
            "02n" -> R.raw.few_clouds
            "03n" -> R.raw.few_clouds
            "04n" -> R.raw.few_clouds
            "09n" -> R.raw.rain_anim
            "10n" -> R.raw.rain_anim
            "11n" -> R.raw.thunderstorm
            "13n" -> R.raw.snow_anim
            "50n" -> R.raw.mist
            else -> R.raw.clear_sky_anim
        }
        animationView.setAnimation(animationResId)
        animationView.playAnimation()
    }

}