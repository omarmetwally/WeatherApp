package com.omarinc.weather.utilities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Geocoder
import android.os.Build
import android.util.Log
import android.view.View
import com.omarinc.weather.model.WeatherResponse
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
    fun setLocationNameByGeoCoder(weatherResponse: WeatherResponse, context: Context): String {
        try {
            val x =
                Geocoder(context).getFromLocation(
                    weatherResponse.city.coord.lat,
                    weatherResponse.city.coord.lon,
                    5
                )

            return return x?.get(0)?.locality ?: " "

        } catch (e: Exception) {
            Log.e(TAG, "setLocationNameByGeoCoder: ", )
        }
        return ""
    }
}