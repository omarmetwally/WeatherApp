package com.omarinc.weather.utilities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
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
import com.omarinc.weather.R
import com.omarinc.weather.alert.services.WeatherNotificationWorker
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.WeatherResponse
import com.omarinc.weather.sharedpreferences.SharedPreferencesImpl
import java.io.IOException
import java.util.Locale

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


}