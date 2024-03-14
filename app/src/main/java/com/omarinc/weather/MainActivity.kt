package com.omarinc.weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.omarinc.weather.currentHomeWeather.view.MyDailyAdapter
import com.omarinc.weather.currentHomeWeather.view.MyHourAdapter
import com.omarinc.weather.currentHomeWeather.view.setIcon
import com.omarinc.weather.currentHomeWeather.viewmodel.CurrentWeatherViewModel
import com.omarinc.weather.currentHomeWeather.viewmodel.ViewModelFactory
import com.omarinc.weather.databinding.ActivityMainBinding
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.ForecastEntry
import com.omarinc.weather.model.WeatherRepositoryImpl
import com.omarinc.weather.model.WeatherResponse
import com.omarinc.weather.network.ApiState
import com.omarinc.weather.network.WeatherRemoteDataSourceImpl
import com.omarinc.weather.utilities.Constants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        }
        binding.rvHours.apply {
            adapter = myHourAdapter
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

                        Log.i(TAG, "observeWeather: sus ${result.weatherResponse}")
                        viewModel.extractFiveDayForecast(result.weatherResponse.list)
                        viewModel.extractTodayForecast(result.weatherResponse.list)
                        setVisibility()
                        setupRecyclerView()
                        currentForecastUI(result.weatherResponse)
                        detailsForecastUI(result.weatherResponse.list)
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



    fun setVisibility()
    {
        binding.loadingLottie.visibility=View.GONE
        binding.ivLocation.visibility=View.VISIBLE
        binding.tvLocationName.visibility=View.VISIBLE
        binding.tvDate.visibility=View.VISIBLE
        binding.ivWeather.visibility=View.VISIBLE

        binding.tvCurrentDegree.visibility=View.VISIBLE
        binding.tvWeatherStatus.visibility=View.VISIBLE

        binding.cvDetails.visibility=View.VISIBLE




    }

    fun currentForecastUI(weather: WeatherResponse)
    {

        val currentDate = SimpleDateFormat("EEE, d MMM -yy", Locale.getDefault()).format(Date())
        binding.tvLocationName.text= "${weather.city.name} ,${weather.city.country}"
        binding.tvDate.text="$currentDate"
        setIcon(viewModel.todayForecast.value.get(0).icon,binding.ivWeather)

        binding.tvCurrentDegree.text="${viewModel.todayForecast.value.get(0).temp.toInt()}°C"
        binding.tvWeatherStatus.text="${viewModel.todayForecast.value.get(0).condition}\nFeels like ${viewModel.todayForecast.value.get(0).temp.toInt()}°C"

        var date = Date(weather.city.sunrise * 1000)

        val time = SimpleDateFormat("hh:mm a", Locale.getDefault())

        binding.tvDynamicSunrise.text="${time.format(date)}"
        date= Date(weather.city.sunset * 1000)
        binding.tvDynamicSunset.text="${time.format(date)}"


        Log.i(TAG, "currentForecastUI: ${weather.city.coord.lat} "+weather.city.coord.lon)
    }

    fun detailsForecastUI(list: List<ForecastEntry>)
    {
        binding.tvDynamicPressure.text="${list.get(0).main.pressure}mb"
        binding.tvDynamicHumidity.text="${list.get(0).main.humidity}%"
        binding.tvDynamicWind.text="${list.get(0).wind.speed}m/s"
        binding.tvDynamicCloud.text="${list.get(0).clouds.all}%"


    }

}