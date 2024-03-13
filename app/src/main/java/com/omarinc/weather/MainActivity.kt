package com.omarinc.weather

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.omarinc.weather.currentHomeWeather.view.MyDailyAdapter
import com.omarinc.weather.currentHomeWeather.view.MyHourAdapter
import com.omarinc.weather.currentHomeWeather.viewmodel.CurrentWeatherViewModel
import com.omarinc.weather.currentHomeWeather.viewmodel.ViewModelFactory
import com.omarinc.weather.databinding.ActivityMainBinding
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.WeatherRepositoryImpl
import com.omarinc.weather.network.ApiState
import com.omarinc.weather.network.WeatherRemoteDataSourceImpl
import com.omarinc.weather.utilities.Constants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: CurrentWeatherViewModel
    private val myDailyAdapter = MyDailyAdapter()
    private val myHourAdapter = MyHourAdapter()
    private lateinit var binding: ActivityMainBinding


    lateinit var  coordinates:Coordinates
    private  val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory=ViewModelFactory(WeatherRepositoryImpl.getInstance(
            WeatherRemoteDataSourceImpl.getInstance(this)
        ),this)
        viewModel=ViewModelProvider(this,factory).get(CurrentWeatherViewModel::class.java)

        coordinates=Coordinates(0.0,0.0)



        observeWeather()


    }

    private fun setupRecyclerView() {
        binding.rvDays.apply {
            adapter = myDailyAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        binding.rvHours.apply {
            adapter = myHourAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }


        lifecycleScope.launch{ viewModel.fiveDayForecast.collectLatest { forecastList ->

            Log.i(TAG, "fiveDayForecast: ")

            binding.rvDays.visibility= View.VISIBLE
            myDailyAdapter.submitList(forecastList)
        } }

        lifecycleScope.launch{ viewModel.todayForecast.collectLatest { forecastList ->

            Log.i(TAG, "fiveDayForecast: ")

            binding.rvHours.visibility= View.VISIBLE
            myHourAdapter.submitList(forecastList)
        } }

    }

    private fun observeWeather()  {
        lifecycleScope.launch {
            viewModel.weather.collectLatest { result ->
                when (result) {
                    is ApiState.Loading -> {
                        Log.i(TAG, "observeWeather: loading")
                    }

                    is ApiState.Success -> {

                        binding.loadingLottie.visibility=View.GONE
                        Log.i(TAG, "observeWeather: sus ${result.weatherResponse}")
                        viewModel.extractFiveDayForecast(result.weatherResponse.list)
                        viewModel.extractTodayForecast(result.weatherResponse.list)
                        setupRecyclerView()
                        Log.i(TAG, "extractFiveDayForecast: sus ${viewModel.fiveDayForecast.value}")
                        Log.i(TAG, "extractFiveDayForecast: sus ${viewModel.todayForecast.value}")

                    }

                    is ApiState.Failure -> {

                        Log.i(TAG, "observeWeather: ${result.msg}")
                    }
                }
            }
        }





    }


    override fun onStart() {
        super.onStart()

        if (checkPermissions()) {
//            getLocation()
            viewModel.startLocationUpdates()
        } else {
            requestLocationPermissions()
        }
    }
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ), Constants.LocationPermissionID)
    }

    private fun checkPermissions(): Boolean {
        var result = false
        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
            ||
            (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED))
        {
            result = true
        }
        return result

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.LocationPermissionID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                viewModel.startLocationUpdates()
            }
        }
    }


}