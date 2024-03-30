package com.omarinc.weather.currentHomeWeather.view

import android.Manifest
import android.content.pm.PackageManager
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
import com.omarinc.weather.R
import com.omarinc.weather.currentHomeWeather.viewmodel.HomeViewModel
import com.omarinc.weather.currentHomeWeather.viewmodel.ViewModelFactory
import com.omarinc.weather.databinding.FragmentHomeBinding
import com.omarinc.weather.db.WeatherLocalDataSourceImpl
import com.omarinc.weather.model.Coordinates
import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.ForecastEntry
import com.omarinc.weather.model.WeatherRepositoryImpl
import com.omarinc.weather.model.WeatherResponse
import com.omarinc.weather.network.ApiState
import com.omarinc.weather.network.DataBaseState
import com.omarinc.weather.network.WeatherRemoteDataSourceImpl
import com.omarinc.weather.settings.SettingViewModel
import com.omarinc.weather.sharedpreferences.SharedPreferencesImpl
import com.omarinc.weather.utilities.Constants
import com.omarinc.weather.utilities.Helpers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val myDailyAdapter = MyDailyAdapter()
    private val myHourAdapter = MyHourAdapter()
    private lateinit var viewModel: HomeViewModel
    private lateinit var settingViewModel: SettingViewModel
    private var flag: Boolean = false

    companion object {
        fun newInstance() = HomeFragment()
    }


    private val TAG = "HomeFragment"
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
                WeatherLocalDataSourceImpl.getInstance(requireContext()),
                SharedPreferencesImpl.getInstance(requireContext())
            ),
            requireActivity()
        )
        viewModel = ViewModelProvider(requireActivity(), factory).get(HomeViewModel::class.java)
        settingViewModel =
            ViewModelProvider(requireActivity(), factory).get(SettingViewModel::class.java)

        setupRecyclerView()
        observeWeather()
        setVisibility(false)


//        if (checkPermissions()) {
//            viewModel.startLocationUpdates(viewModelSettings.readStringFromSharedPreferences(Constants.KEY_LANGUAGE),"")
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


        lifecycleScope.launch {
            viewModel.fiveDayForecast.collectLatest { forecastList ->

                Log.i(TAG, "fiveDayForecast: ")

                binding.rvDays.visibility = View.VISIBLE
                myDailyAdapter.submitList(forecastList)
            }
        }

        lifecycleScope.launch {
            viewModel.todayForecast.collectLatest { forecastList ->

                Log.i(TAG, "fiveDayForecast: ")

                binding.rvHours.visibility = View.VISIBLE
                myHourAdapter.submitList(forecastList)
            }
        }

    }

    private fun observeWeather() {
        lifecycleScope.launch {
            viewModel.weather.collectLatest { result ->
                when (result) {
                    is ApiState.Loading -> {
                        Log.i(TAG, "observeWeather: loading")
                        setVisibility(false)
                    }

                    is ApiState.Success -> {
                        if (flag) {
                            viewModel.deleteCashedData()
                            Log.i(TAG, "flag: ${result.weatherResponse}")
                            viewModel.insertCashedData(result.weatherResponse)
                        }

                        Log.i(TAG, "observeWeather: sus ${result.weatherResponse}")
                        viewModel.extractFiveDayForecast(result.weatherResponse.list)
                        viewModel.extractTodayForecast(result.weatherResponse.list)

                        setupRecyclerView()
                        detailsForecastUI(result.weatherResponse.list)
                        currentForecastUI(result.weatherResponse)
                        setVisibility(true)
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
            handleWeatherUpdatesBasedOnNetwork()
        } else {
            requestLocationPermissions()
        }

    }


    private fun handleWeatherUpdatesBasedOnNetwork() {
        val city = arguments?.let { HomeFragmentArgs.fromBundle(it).city }
        if (Helpers.isNetworkConnected(requireContext())) {
            handleWeatherUpdatesForCity(city)
        } else {
            loadWeatherFromDB()
        }
    }

    private fun handleWeatherUpdatesForCity(city: FavoriteCity?) {
        if (city != null) {
            processWeatherForSpecificCity(city)
        } else {
            decideGpsOrMap()
        }
    }

    private fun decideGpsOrMap() {
        flag = true

        if (settingViewModel.readStringFromSharedPreferences(Constants.KEY_LOCATION_SOURCE)==Constants.VALUE_MAP)
        {
            Log.i(TAG, "decideGpsOrMap: ${settingViewModel.readCoordinatesFromSharedPreferences(Constants.LATITUDE_KEY)} -- ${settingViewModel.readCoordinatesFromSharedPreferences(Constants.LONGITUDE_KEY)}")

            viewModel.setCoordinates(Coordinates(lat = settingViewModel.readCoordinatesFromSharedPreferences(Constants.LATITUDE_KEY),
                lon = settingViewModel.readCoordinatesFromSharedPreferences(Constants.LONGITUDE_KEY)))
            viewModel.getCurrentWeather(
                Coordinates(
                    lat = settingViewModel.readCoordinatesFromSharedPreferences(Constants.LATITUDE_KEY),
                    lon = settingViewModel.readCoordinatesFromSharedPreferences(Constants.LONGITUDE_KEY)),
                settingViewModel.readStringFromSharedPreferences(Constants.KEY_LANGUAGE),
                "metric"
            )
        }
        else{
            viewModel.startLocationUpdates(
                settingViewModel.readStringFromSharedPreferences(Constants.KEY_LANGUAGE),
                "metric"
            )
        }
    }

    private fun processWeatherForSpecificCity(city: FavoriteCity) {
        flag = false
        Log.d(TAG, "City received: ${city.cityName}")
        viewModel.setCoordinates(Coordinates(lat = city.latitude, lon = city.longitude))
        viewModel.getCurrentWeather(
            Coordinates(lat = city.latitude, lon = city.longitude),
            settingViewModel.readStringFromSharedPreferences(Constants.KEY_LANGUAGE),
            "metric"
        )
    }

    private fun loadWeatherFromDB() {
        viewModel.getCashedData()

        lifecycleScope.launch {
            viewModel.weatherDB.collectLatest { result ->
                if (result!=null)
                {

                    viewModel.extractFiveDayForecast(result.list)
                    viewModel.extractTodayForecast(result.list)

                    setupRecyclerView()
                    detailsForecastUI(result.list)
                    currentForecastUI(result)
                    setVisibility(true)

                }else{
                    Log.i(TAG, "setupRecyclerView: load")
                    setVisibility(false)
                }

            }
        }
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), Constants.LocationPermissionID
        )
    }

    private fun checkPermissions(): Boolean {
        var result = false
        if ((ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
            ||
            (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
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
                viewModel.startLocationUpdates(
                    settingViewModel.readStringFromSharedPreferences(Constants.KEY_LANGUAGE),
                    "metric"
                )
            }
        }
    }


    fun setVisibility(visibility: Boolean) {

        if (visibility) {
            binding.loadingLottie.visibility = View.GONE
            binding.ivLocation.visibility = View.VISIBLE
            binding.tvLocationName.visibility = View.VISIBLE
            binding.tvDate.visibility = View.VISIBLE
            binding.ivWeather.visibility = View.VISIBLE

            binding.tvCurrentDegree.visibility = View.VISIBLE
            binding.tvWeatherStatus.visibility = View.VISIBLE
            binding.cvDetails.visibility = View.VISIBLE
            binding.rvHours.visibility = View.VISIBLE
            binding.rvDays.visibility = View.VISIBLE
        } else {
            binding.loadingLottie.visibility = View.VISIBLE
            binding.ivLocation.visibility = View.GONE
            binding.tvLocationName.visibility = View.GONE
            binding.tvDate.visibility = View.GONE
            binding.ivWeather.visibility = View.GONE

            binding.tvCurrentDegree.visibility = View.GONE
            binding.tvWeatherStatus.visibility = View.GONE
            binding.cvDetails.visibility = View.GONE
            binding.rvHours.visibility = View.GONE
            binding.rvDays.visibility = View.GONE

        }


    }

    fun currentForecastUI(weather: WeatherResponse) {
        val city = arguments?.let { HomeFragmentArgs.fromBundle(it).city }
        val currentDate = SimpleDateFormat("EEE, d MMM -yy", Locale.getDefault()).format(Date())
        var coordinates: Coordinates = viewModel.getCoordinates()



        if (Helpers.isNetworkConnected(requireContext())) {

            if (city != null) {
                binding.tvLocationName.text = "${city.cityName}"

            } else {
                binding.tvLocationName.text =
                    "${Helpers.setLocationNameByGeoCoder(coordinates, requireContext())}"

                viewModel.writeCityToSharedPreferences(
                    Constants.KEY_Ciy_Name,
                    binding.tvLocationName.text.toString()
                )
            }
        } else {
            binding.tvLocationName.text =
                "${viewModel.readCityFromSharedPreferences(Constants.KEY_Ciy_Name)}"

        }


//        binding.tvLocationName.text = "${weather.city.name} ,${weather.city.country}"
        binding.tvDate.text = "$currentDate"
        Log.i(TAG, "currentForecastUI: ${viewModel.todayForecast.value}")
        if (viewModel.todayForecast.value.isNotEmpty()) {
            val firstForecast = viewModel.todayForecast.value[0]
            Helpers.setLottieAnimation(firstForecast.icon, binding.ivWeather)
            binding.tvCurrentDegree.text = "${firstForecast.temp}"
            binding.tvWeatherStatus.text = firstForecast.condition
        } else {
            Log.d(TAG, "No forecasts available")
        }

        var date = Date(weather.city.sunrise * 1000)

        val time = SimpleDateFormat("hh:mm a", Locale.getDefault())

        binding.tvDynamicSunrise.text = "${time.format(date)}"
        date = Date(weather.city.sunset * 1000)
        binding.tvDynamicSunset.text = "${time.format(date)}"


        Log.i(TAG, "currentForecastUI: ${weather.city.coord.lat} " + weather.city.coord.lon)

    }

    fun detailsForecastUI(list: List<ForecastEntry>) {
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

        val pressure = numberFormat.format(list[0].main.pressure)
        val humidity = numberFormat.format(list[0].main.humidity)
        var windSpeed = numberFormat.format(list[0].wind.speed)
        val cloudiness = numberFormat.format(list[0].clouds.all)

        binding.tvDynamicPressure.text = "${pressure} ${getString(R.string.hpa)}"
        binding.tvDynamicHumidity.text = "${humidity}%"
        binding.tvDynamicCloud.text = "${cloudiness}%"
        if (SharedPreferencesImpl.getInstance(requireContext())
                .readStringFromSharedPreferences(Constants.KEY_WIND_SPEED_UNIT) == Constants.VALUE_MILE_HOUR
        ) {
            windSpeed = numberFormat.format(Helpers.meterPerSecondToMilePerHour(list[0].wind.speed))
            binding.tvDynamicWind.text = "${windSpeed} ${getString(R.string.mile_per_hour)}"
        } else {
            binding.tvDynamicWind.text = "${windSpeed} ${getString(R.string.meter_per_sec)}"
        }


    }
}