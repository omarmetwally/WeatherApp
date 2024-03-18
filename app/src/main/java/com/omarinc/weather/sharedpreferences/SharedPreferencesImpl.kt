package com.omarinc.weather.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.omarinc.weather.utilities.Constants


class SharedPreferencesImpl private constructor(context: Context) :ISharedPreferences {
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
        var value :String
        sharedPreferences.getString(key, "null").let {
            value = it ?: "null"
        }
        return value    }
}