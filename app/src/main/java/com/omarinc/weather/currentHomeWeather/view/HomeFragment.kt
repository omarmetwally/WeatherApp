package com.omarinc.weather.currentHomeWeather.view

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.omarinc.weather.R
import com.omarinc.weather.currentHomeWeather.viewmodel.HomeViewModel
import com.omarinc.weather.currentHomeWeather.viewmodel.ViewModelFactory
import com.omarinc.weather.databinding.FragmentHomeBinding
import com.omarinc.weather.db.WeatherLocalDataSourceImpl
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

class HomeFragment : Fragment() {

    private  lateinit var binding: FragmentHomeBinding
    private val myDailyAdapter = MyDailyAdapter()
    private val myHourAdapter = MyHourAdapter()
    private lateinit var viewModel: HomeViewModel

    companion object {
        fun newInstance() = HomeFragment()
    }


    private  val TAG = "HomeFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                WeatherRemoteDataSourceImpl.getInstance(requireContext()),
                WeatherLocalDataSourceImpl.getInstance(requireContext())
            ),
            requireActivity()
        )
        viewModel = ViewModelProvider(requireActivity(), factory).get(HomeViewModel::class.java)

        setupRecyclerView()
        observeWeather()

//
//        if (checkPermissions()) {
//            viewModel.startLocationUpdates()
//        } else {
//            requestLocationPermissions()
//        }
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
        val city = arguments?.let { HomeFragmentArgs.fromBundle(it).city }

        if (checkPermissions()) {
//            getLocation()

            if (city != null) {
                // Use the city argument as needed, for example:
                Log.d(TAG, "City received: ${city.cityName}")
                viewModel.getCurrentWeather(Coordinates(lat = city.latitude, lon = city.longitude), "en","metric")

            }else {
                viewModel.startLocationUpdates()

            }
        } else {
            requestLocationPermissions()
        }
    }
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ), Constants.LocationPermissionID)
    }

    private fun checkPermissions(): Boolean {
        var result = false
        if ((ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
            ||
            (ContextCompat.checkSelfPermission(requireContext(),
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
        var coordinates: Coordinates = viewModel.getCityName()
        val geocoder = Geocoder(requireContext(), Locale.getDefault())



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