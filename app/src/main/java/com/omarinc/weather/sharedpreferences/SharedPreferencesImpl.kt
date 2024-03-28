package com.omarinc.weather.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.omarinc.weather.utilities.Constants
import kotlinx.coroutines.flow.Flow


class SharedPreferencesImpl private constructor(context: Context) : ISharedPreferences {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(Constants.SETTINGS, AppCompatActivity.MODE_PRIVATE)
    }

    companion object {
        private var instance: SharedPreferencesImpl? = null

        fun getInstance(context: Context): SharedPreferencesImpl {
            return instance ?: synchronized(this) {
                instance ?: SharedPreferencesImpl(context).also { instance = it }
            }
        }
    }


    override fun writeStringToSharedPreferences(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun readStringFromSharedPreferences(key: String): String {
        var value: String
        sharedPreferences.getString(key, "null").let {
            value = it ?: "null"
        }
        return value

    }

    override fun writeCoordinatesToSharedPreferences(key: String, value: Double) {
        sharedPreferences.edit().putLong(key, java.lang.Double.doubleToRawLongBits(value)).apply()
    }

    //    override fun readCoordinatesFromSharedPreferences(key: String): Double {
//        var value :Double
//        sharedPreferences.getLong(key, java.lang.Double.doubleToRawLongBits(0.0)).let {
//            value = (it ?: 0.0).toDouble()
//        }
//        return value
//
//    }
    override fun readCoordinatesFromSharedPreferences(key: String): Double {
        val defaultValue = java.lang.Double.doubleToRawLongBits(0.0)
        val longBits = sharedPreferences.getLong(key, defaultValue)
        return java.lang.Double.longBitsToDouble(longBits)
    }

}