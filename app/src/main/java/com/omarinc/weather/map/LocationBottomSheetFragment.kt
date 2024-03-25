package com.omarinc.weather.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.omarinc.weather.R
import com.omarinc.weather.alert.viewmodel.AlertViewModel
import com.omarinc.weather.alert.workmanger.WeatherNotificationWorker
import com.omarinc.weather.currentHomeWeather.viewmodel.HomeViewModel
import com.omarinc.weather.currentHomeWeather.viewmodel.ViewModelFactory
import com.omarinc.weather.databinding.FragmentFavoriteBinding
import com.omarinc.weather.databinding.FragmentLocationBottomSheetBinding
import com.omarinc.weather.db.WeatherLocalDataSourceImpl
import com.omarinc.weather.favorite.viewmodel.FavoriteViewModel
import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.WeatherAlert
import com.omarinc.weather.model.WeatherRepositoryImpl
import com.omarinc.weather.network.WeatherRemoteDataSourceImpl
import com.omarinc.weather.sharedpreferences.SharedPreferencesImpl
import com.omarinc.weather.utilities.Constants
import java.util.concurrent.TimeUnit

class LocationBottomSheetFragment(val location:String,val lat: Double, val lng: Double,val fragmentType:String="",val timeStamp:Long=0) : BottomSheetDialogFragment() {

    lateinit var binding:FragmentLocationBottomSheetBinding
    private lateinit var viewModel: FavoriteViewModel
    private lateinit var alertViewModel: AlertViewModel
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


        if (fragmentType==Constants.FRAGMENT_TYPE)
        {
            alertViewModel = ViewModelProvider(requireActivity(), factory).get(AlertViewModel::class.java)
            registerNotification();
            binding.btnSaveLocation.setOnClickListener {
                alertViewModel.insertAlert(WeatherAlert(locationName = location, latitude = lat, longitude = lng, alertTime = timeStamp))
                findNavController().navigateUp()
                dismiss()
            }


        }
        else{
            viewModel = ViewModelProvider(requireActivity(), factory).get(FavoriteViewModel::class.java)

            binding.btnSaveLocation.setOnClickListener {
                viewModel.insertFavorite(FavoriteCity(cityName = location, latitude = lat, longitude = lng))
                findNavController().navigateUp()
                dismiss()
            }
        }




    }

    private fun registerNotification() {
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

            WorkManager.getInstance(requireContext()).enqueue(workRequest)

        }

    }
    companion object {
        const val TAG = "LocationBottomSheetFragment"
    }
}
