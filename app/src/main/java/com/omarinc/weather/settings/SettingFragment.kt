package com.omarinc.weather.settings

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.layoutDirection
import com.omarinc.weather.R
import com.omarinc.weather.currentHomeWeather.viewmodel.HomeViewModel
import com.omarinc.weather.currentHomeWeather.viewmodel.ViewModelFactory
import com.omarinc.weather.databinding.FragmentHomeBinding
import com.omarinc.weather.databinding.FragmentSettingBinding
import com.omarinc.weather.db.WeatherLocalDataSourceImpl
import com.omarinc.weather.model.WeatherRepositoryImpl
import com.omarinc.weather.network.WeatherRemoteDataSourceImpl
import com.omarinc.weather.sharedpreferences.SharedPreferencesImpl
import com.omarinc.weather.utilities.Constants
import com.omarinc.weather.utilities.Helpers
import java.util.Locale

class SettingFragment : Fragment() {

    companion object {
        fun newInstance() = SettingFragment()
    }

    private lateinit var viewModel: SettingViewModel
    private lateinit var binding: FragmentSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
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
        viewModel = ViewModelProvider(requireActivity(), factory).get(SettingViewModel::class.java)

        loadSettings()




        binding.rgLocation.setOnCheckedChangeListener { _, checkedId ->
            val source = when (checkedId) {
                R.id.rbMap -> Constants.VALUE_MAP
                R.id.rbGps -> Constants.VALUE_GPS
                else -> Constants.VALUE_GPS
            }
            viewModel.writeStringToSharedPreferences(Constants.KEY_LOCATION_SOURCE, source)
        }

        binding.rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            val source = when (checkedId) {
                R.id.rbArabic -> Constants.VALUE_ARABIC
                R.id.rbEnglish -> Constants.VALUE_ENGLISH
                else -> Constants.VALUE_ENGLISH
            }
            viewModel.writeStringToSharedPreferences(Constants.KEY_LANGUAGE, source)
            Helpers.setAppLanguage(requireActivity(),source)
//            reloadApp()
        }

        binding.rgTemp.setOnCheckedChangeListener { _, checkedId ->
            val source = when (checkedId) {
                R.id.rbCelsius -> Constants.VALUE_CELSIUS
                R.id.rbFahrenheit -> Constants.VALUE_FAHRENHEIT
                R.id.rbKelvin -> Constants.VALUE_KELVIN
                else -> Constants.VALUE_CELSIUS
            }
            viewModel.writeStringToSharedPreferences(Constants.KEY_TEMPERATURE_UNIT, source)
//            requireActivity().recreate()
        }

        binding.rgWindSpeed.setOnCheckedChangeListener { _, checkedId ->
            val source = when (checkedId) {
                R.id.rbMeter -> Constants.VALUE_METER_SEC
                R.id.rbMile -> Constants.VALUE_MILE_HOUR

                else -> Constants.VALUE_METER_SEC
            }
            viewModel.writeStringToSharedPreferences(Constants.KEY_WIND_SPEED_UNIT, source)
        }

        binding.rgNotification.setOnCheckedChangeListener { _, checkedId ->
            val source = when (checkedId) {
                R.id.rbEnable -> {
                    openAppNotificationSettings()
                    Constants.VALUE_ENABLE
                }
                R.id.rbDisable ->

                {
                    openAppNotificationSettings()
                    Constants.VALUE_DISABLE
                }

                else -> Constants.VALUE_DISABLE
            }
            viewModel.writeStringToSharedPreferences(Constants.KEY_NOTIFICATIONS_ENABLED, source)
        }


    }

    private fun loadSettings() {
        when (viewModel.readStringFromSharedPreferences(Constants.KEY_LOCATION_SOURCE)) {
            Constants.VALUE_MAP -> binding.rbMap.isChecked = true
            Constants.VALUE_GPS -> binding.rbGps.isChecked = true
            else -> binding.rbGps.isChecked = true
        }

        when (viewModel.readStringFromSharedPreferences(Constants.KEY_LANGUAGE)) {
            Constants.VALUE_ARABIC -> binding.rbArabic.isChecked = true
            Constants.VALUE_ENGLISH -> binding.rbEnglish.isChecked = true
            else -> binding.rbEnglish.isChecked = true
        }

        when (viewModel.readStringFromSharedPreferences(Constants.KEY_TEMPERATURE_UNIT)) {
            Constants.VALUE_CELSIUS -> binding.rbCelsius.isChecked = true
            Constants.VALUE_FAHRENHEIT -> binding.rbFahrenheit.isChecked = true
            Constants.VALUE_KELVIN -> binding.rbKelvin.isChecked = true
            else -> binding.rbCelsius.isChecked = true
        }

        when (viewModel.readStringFromSharedPreferences(Constants.KEY_WIND_SPEED_UNIT)) {
            Constants.VALUE_METER_SEC -> binding.rbMeter.isChecked = true
            Constants.VALUE_MILE_HOUR -> binding.rbMile.isChecked = true
            else -> binding.rbMeter.isChecked = true
        }

        when (viewModel.readStringFromSharedPreferences(Constants.KEY_NOTIFICATIONS_ENABLED)) {
            Constants.VALUE_ENABLE -> binding.rbEnable.isChecked = true
            Constants.VALUE_DISABLE -> binding.rbDisable.isChecked = true
            else -> binding.rbDisable.isChecked = true
        }
    }


//    fun setAppLanguage(activity: Activity, languageCode: String) {
//        val locale = Locale(languageCode)
//        Locale.setDefault(locale)
//        val resources: Resources = activity.resources
//        val config: Configuration = resources.configuration
//        config.setLocale(locale)
//        resources.updateConfiguration(config, resources.displayMetrics)
//    }


    private fun openAppNotificationSettings() {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    putExtra("app_package", requireContext().packageName)
                    putExtra("app_uid", requireContext().applicationInfo.uid)
                }
                else -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = Uri.parse("package:${requireContext().packageName}")
                }
            }
        }
        startActivity(intent)
    }


}