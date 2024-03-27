package com.omarinc.weather.alert.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.work.ListenableWorker
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.omarinc.weather.R
import com.omarinc.weather.db.WeatherLocalDataSourceImpl
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.WeatherRepositoryImpl
import com.omarinc.weather.network.WeatherRemoteDataSourceImpl
import com.omarinc.weather.sharedpreferences.SharedPreferencesImpl
import com.omarinc.weather.utilities.Constants
import com.omarinc.weather.utilities.Helpers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.log


class AlarmReceiver : BroadcastReceiver() {
    private  val TAG = "AlarmReceiver"
    @SuppressLint("InflateParams")
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {

            val updatedContext = Helpers.updateLocale(it)


            val latitude = intent?.getDoubleExtra(Constants.LATITUDE_KEY, 0.0) ?: 0.0
            val longitude = intent?.getDoubleExtra(Constants.LONGITUDE_KEY, 0.0) ?: 0.0
            val location = intent?.getStringExtra(Constants.LOCATION_NAME_KEY) ?: "Unknown location"
            val repository = WeatherRepositoryImpl.getInstance(
                WeatherRemoteDataSourceImpl.getInstance(updatedContext),
                WeatherLocalDataSourceImpl.getInstance(updatedContext),
                SharedPreferencesImpl.getInstance(updatedContext)
            )

//            val serviceIntent = Intent(it, FetchWeatherService::class.java).apply {
//                putExtras(intent!!.extras!!)
//            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                Log.i(TAG, "onReceive: IFFF")
//                it.startService(serviceIntent)
//            } else {
//                Log.i(TAG, "onReceive: Else")
//                it.startService(serviceIntent)
//            }


            CoroutineScope(Dispatchers.IO).launch {
                try {
                    try {
                        val response = repository.getWeatherResponse(Coordinates(latitude,longitude), SharedPreferencesImpl.getInstance(context = it).readStringFromSharedPreferences(Constants.KEY_LANGUAGE), "metric").first()
                        if (response.isSuccessful) {
                            response.body()?.let { weatherResponse ->
                                val weatherCondition = weatherResponse.list.first().weather.first().description

                                withContext(Dispatchers.Main)
                                {
                                    val windowManager = it.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                                    val layoutParams = WindowManager.LayoutParams(
                                        WindowManager.LayoutParams.WRAP_CONTENT,
                                        WindowManager.LayoutParams.WRAP_CONTENT,
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                        PixelFormat.TRANSLUCENT
                                    )

                                    layoutParams.gravity = Gravity.CENTER
                                    layoutParams.x = 0
                                    layoutParams.y = 0

                                    val inflater = updatedContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                                    val floatyView = inflater.inflate(R.layout.alert_dialog, null)

                                    val tvWeather = floatyView.findViewById<TextView>(R.id.alertText)

                                    tvWeather.text= "${updatedContext.getString(R.string.weather_Condition)} $location: $weatherCondition"


                                    Glide.with(updatedContext)
                                        .load(R.drawable.mix_weather)
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .into(floatyView.findViewById(R.id.imageView2))
                                    val button = floatyView.findViewById<Button>(R.id.dismissButton)
                                    button.setOnClickListener {
                                        windowManager.removeView(floatyView)
                                    }

                                    playAlertSound(updatedContext)
                                    windowManager.addView(floatyView, layoutParams)
                                }

                                ListenableWorker.Result.success()
                            } ?: ListenableWorker.Result.failure()
                        } else {
                            ListenableWorker.Result.failure()
                        }
                    } catch (e: Exception) {
                        ListenableWorker.Result.failure()
                    }
                }catch (_: Exception) { }
            }


        }
    }


    private fun playAlertSound(context: Context?) {
        context?.let { ctx ->

            Log.i(TAG, "playAlertSound: ")
            val mediaPlayer = MediaPlayer.create(ctx, R.raw.alert_sound)
            mediaPlayer.setOnCompletionListener { mp -> mp.release() }
            mediaPlayer.start()
        }
    }
}


//
// lw hst5dm background Service
//class FetchWeatherService: Service(){
//    private  val TAG = "AlarmReceiver"
//    override fun onBind(p0: Intent?): IBinder? {
//        return null
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val latitude = intent?.getDoubleExtra(Constants.LATITUDE_KEY, 0.0) ?: 0.0
//        val longitude = intent?.getDoubleExtra(Constants.LONGITUDE_KEY, 0.0) ?: 0.0
//        val location = intent?.getStringExtra(Constants.LOCATION_NAME_KEY) ?: "Unknown location"
//
//        Log.i(TAG, "onStartCommand: ")
//        fetchWeatherAndShowAlert(latitude, longitude, location)
//
//        return START_NOT_STICKY
//    }
//
//
//    private fun fetchWeatherAndShowAlert(latitude: Double, longitude: Double, location: String) {
//        val repository = WeatherRepositoryImpl.getInstance(
//            WeatherRemoteDataSourceImpl.getInstance(applicationContext),
//            WeatherLocalDataSourceImpl.getInstance(applicationContext),
//            SharedPreferencesImpl.getInstance(applicationContext)
//        )
//
//        Log.i(TAG, "fetchWeatherAndShowAlert: ")
//        CoroutineScope(Dispatchers.IO).launch {
//
//
//            try {
//                val response = repository.getWeatherResponse(Coordinates(latitude,longitude), "en", "metric").first()
//                if (response.isSuccessful) {
//                    response.body()?.let { weatherResponse ->
//                        val weatherCondition = weatherResponse.list.first().weather.first().main
//
//                        showAlertOnScreen(weatherCondition)
//
//
//                        ListenableWorker.Result.success()
//                    } ?: ListenableWorker.Result.failure()
//                } else {
//                    ListenableWorker.Result.failure()
//                }
//            } catch (e: Exception) {
//                ListenableWorker.Result.failure()
//            }
//
//
//
//        }
//    }
//
//    private fun showAlertOnScreen(weatherCondition: String) {
//
//        Log.i(TAG, "showAlertOnScreen: ")
//        val windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        val layoutParams = WindowManager.LayoutParams(
//            WindowManager.LayoutParams.WRAP_CONTENT,
//            WindowManager.LayoutParams.WRAP_CONTENT,
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
//            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//            PixelFormat.TRANSLUCENT
//        )
//
//        layoutParams.gravity = Gravity.CENTER
//        layoutParams.x = 0
//        layoutParams.y = 0
//
//        val inflater = application.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val floatyView = inflater.inflate(R.layout.alert_dialog, null)
//
//        val button = floatyView.findViewById<Button>(R.id.dismissButton)
//        button.setOnClickListener {
//            windowManager.removeView(floatyView)
//        }
//
//        windowManager.addView(floatyView, layoutParams)
//
//    }
//
//}