package com.omarinc.weather.map

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.omarinc.weather.R
import com.omarinc.weather.alert.viewmodel.AlertViewModel
import com.omarinc.weather.alert.services.AlarmReceiver
import com.omarinc.weather.alert.services.WeatherNotificationWorker
import com.omarinc.weather.currentHomeWeather.viewmodel.ViewModelFactory
import com.omarinc.weather.databinding.FragmentLocationBottomSheetBinding
import com.omarinc.weather.db.WeatherLocalDataSourceImpl
import com.omarinc.weather.favorite.viewmodel.FavoriteViewModel
import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.WeatherAlert
import com.omarinc.weather.model.WeatherRepositoryImpl
import com.omarinc.weather.network.WeatherRemoteDataSourceImpl
import com.omarinc.weather.settings.SettingViewModel
import com.omarinc.weather.sharedpreferences.SharedPreferencesImpl
import com.omarinc.weather.utilities.Constants
import java.util.Random
import java.util.concurrent.TimeUnit

class LocationBottomSheetFragment(val location:String,val lat: Double, val lng: Double,val fragmentType:String="",val timeStamp:Long=0) : BottomSheetDialogFragment() {

    lateinit var binding:FragmentLocationBottomSheetBinding
    private lateinit var viewModel: FavoriteViewModel
    private lateinit var alertViewModel: AlertViewModel
    private lateinit var settingsViewModel: SettingViewModel
    private  val TAG = "LocationBottomSheetFrag"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLocationBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = ViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                WeatherRemoteDataSourceImpl.getInstance(requireContext()),
                WeatherLocalDataSourceImpl.getInstance(requireContext()),
                SharedPreferencesImpl.getInstance(requireContext())

            ),
            requireActivity()
        )




        Log.i(TAG, "onViewCreated:${timeStamp} \n ${fragmentType}")
        binding.tvLocationName.text="${getString(R.string.location)}  $location"
        binding.tvLat.text = "${getString(R.string.Latitude)}  $lat"
        binding.tvLng.text = "${getString(R.string.Longitude)} $lng"

        Log.i(TAG, "tesssst: $fragmentType")

        if (fragmentType==Constants.FRAGMENT_TYPE_ALERT)
        {
            Log.i(TAG, "alert")

            alertViewModel = ViewModelProvider(requireActivity(), factory).get(AlertViewModel::class.java)




            binding.btnSaveLocation.setOnClickListener {
                val randomId = (System.currentTimeMillis() / 1000L) + Random().nextInt(9999)
                val weatherAlert = WeatherAlert(id = randomId.toInt(), locationName = location, latitude = lat, longitude = lng, alertTime = timeStamp)

                Log.i(TAG, "Aleeeert ID: ${weatherAlert.id}")
                registerNotification(weatherAlert.id);
                registerAlarm(weatherAlert.id);
                alertViewModel.insertAlert(weatherAlert)
                findNavController().navigateUp()
                dismiss()
            }


        }else if(fragmentType==Constants.FRAGMENT_TYPE_SETTINGS)
        {
            settingsViewModel = ViewModelProvider(requireActivity(), factory).get(SettingViewModel::class.java)

            Log.i(TAG, "settings:")

            Log.i(TAG, "testOmar: $lat -- $lng")
            binding.btnSaveLocation.setOnClickListener {

                settingsViewModel.writeCoordinatesToSharedPreferences(Constants.LONGITUDE_KEY,lng)
                settingsViewModel.writeCoordinatesToSharedPreferences(Constants.LATITUDE_KEY,lat)
                findNavController().navigateUp()
                dismiss()
            }

        }
        else{
            Log.i(TAG, "else: ")

            viewModel = ViewModelProvider(requireActivity(), factory).get(FavoriteViewModel::class.java)

            binding.btnSaveLocation.setOnClickListener {
                viewModel.insertFavorite(FavoriteCity(cityName = location, latitude = lat, longitude = lng))
                findNavController().navigateUp()
                dismiss()
            }
        }




    }


    @SuppressLint("ScheduleExactAlarm")
     fun registerAlarm(alertId: Int) {
        Log.i(TAG, "registerAlarm: $alertId $timeStamp $location")
        val alertTimeMillis = timeStamp
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(Constants.LATITUDE_KEY, lat)
            putExtra(Constants.LONGITUDE_KEY, lng)
            putExtra(Constants.LOCATION_NAME_KEY, location)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alertId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alertTimeMillis,
            pendingIntent
        )
    }


     fun registerNotification(alertId: Int) {
        val alertDelay = timeStamp - System.currentTimeMillis()
        if (alertDelay > 0) {
            val data = Data.Builder()
                .putDouble(Constants.LATITUDE_KEY, lat)
                .putDouble(Constants.LONGITUDE_KEY, lng)
                .putString(Constants.LOCATION_NAME_KEY, location)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<WeatherNotificationWorker>()
                .setInitialDelay(alertDelay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

//            WorkManager.getInstance(requireContext()).enqueue(workRequest)
            WorkManager.getInstance(requireContext()).enqueueUniqueWork("Notification_$alertId", ExistingWorkPolicy.REPLACE, workRequest)


        }

    }
    companion object {
        const val TAG = "LocationBottomSheetFragment"
    }
}
