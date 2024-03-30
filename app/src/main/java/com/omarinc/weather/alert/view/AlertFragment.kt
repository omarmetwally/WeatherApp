package com.omarinc.weather.alert.view

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import com.omarinc.weather.R
import com.omarinc.weather.alert.services.AlarmReceiver
import com.omarinc.weather.alert.viewmodel.AlertViewModel
import com.omarinc.weather.alert.services.WeatherNotificationWorker
import com.omarinc.weather.currentHomeWeather.viewmodel.ViewModelFactory
import com.omarinc.weather.databinding.FragmentAlertBinding
import com.omarinc.weather.db.WeatherLocalDataSourceImpl
import com.omarinc.weather.map.LocationBottomSheetFragment
import com.omarinc.weather.model.WeatherAlert
import com.omarinc.weather.model.WeatherRepositoryImpl
import com.omarinc.weather.network.WeatherRemoteDataSourceImpl
import com.omarinc.weather.settings.SettingViewModel
import com.omarinc.weather.sharedpreferences.SharedPreferencesImpl
import com.omarinc.weather.utilities.Constants
import com.omarinc.weather.utilities.Helpers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar


class AlertFragment : Fragment(), OnAlertClick, OnNotificationClick {


    private lateinit var binding: FragmentAlertBinding
    private lateinit var myAlertAdapter: AlertAdapter
    private lateinit var viewModel: AlertViewModel
    private lateinit var settingsViewModel: SettingViewModel
    private val TAG = "AlertFragment"
    var onClick: OnAlertClick? = null
    var onNotificationClick: OnNotificationClick? = null
    private var notificationStates = mutableMapOf<Int, Boolean>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onClick = this
        onNotificationClick = this
        val factory = ViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                WeatherRemoteDataSourceImpl.getInstance(requireContext()),
                WeatherLocalDataSourceImpl.getInstance(requireContext()),
                SharedPreferencesImpl.getInstance(requireContext())
            ),
            requireActivity()
        )
        viewModel = ViewModelProvider(requireActivity(), factory).get(AlertViewModel::class.java)
        settingsViewModel =
            ViewModelProvider(requireActivity(), factory).get(SettingViewModel::class.java)
        viewModel.getAllAlerts()

        setupRecyclerView()

        binding.fabAddAlert.setOnClickListener {


            if (Helpers.isNetworkConnected(requireContext())) {
                setupNotificationChannel()
                showDateTimePicker()
            } else {
                Snackbar.make(
                    binding.root,
                    "${getString(R.string.no_network)}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun setupRecyclerView() {


        myAlertAdapter = AlertAdapter(
            onDeleteClick = {
                onClick?.onDeleteAlertClick(it)
            },

            onNotificationClick = {

                onNotificationClick?.onNotificationClick(it)

            }


        )





        binding.rvAlerts.apply {
            adapter = myAlertAdapter
        }

        lifecycleScope.launch {
            viewModel.alert.collectLatest { alerts ->
                alerts.forEach { alert ->
                    notificationStates.putIfAbsent(alert.id, alert.isNotificationEnabled)
                }

                if (alerts.isEmpty()) {
                    binding.ivNoLocation.visibility = View.VISIBLE
                    binding.loadingLottie.visibility = View.VISIBLE
                    myAlertAdapter.submitList(alerts)

                } else {
                    binding.ivNoLocation.visibility = View.GONE
                    binding.loadingLottie.visibility = View.GONE
                    myAlertAdapter.submitList(alerts)
                }
            }
        }


    }


//    fun showDateTimePicker() {
//        val currentDate = Calendar.getInstance()
//        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
//            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
//                val selectedDate = Calendar.getInstance()
//                selectedDate.set(year, month, dayOfMonth, hourOfDay, minute)
//                navigateToLocationSelection(selectedDate.timeInMillis)
//            }
//            TimePickerDialog(context, timeSetListener, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show()
//        }
//        DatePickerDialog(requireContext(), dateSetListener, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show()
//    }


    fun showDateTimePicker() {
        val currentDate = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth, hourOfDay, minute)
                navigateToLocationSelection(selectedDate.timeInMillis)
            }

            val timePickerDialog = TimePickerDialog(
                context, timeSetListener, currentDate.get(Calendar.HOUR_OF_DAY),
                currentDate.get(Calendar.MINUTE), false
            )

            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            if (selectedDate.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR) &&
                selectedDate.get(Calendar.YEAR) == currentDate.get(Calendar.YEAR)
            ) {
                timePickerDialog.updateTime(
                    currentDate.get(Calendar.HOUR_OF_DAY),
                    currentDate.get(Calendar.MINUTE)
                )
            }

            timePickerDialog.show()
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(), dateSetListener,
            currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }
        datePickerDialog.show()
    }


    fun navigateToLocationSelection(timeInMillis: Long) {
        Log.i(TAG, "navigateToLocationSelection: $timeInMillis")
        val action = AlertFragmentDirections.actionAlertFragmentToMapFragment()
        action.fragmentType = Constants.FRAGMENT_TYPE_ALERT
        action.timeStamp = timeInMillis

        findNavController().navigate(action)
    }


    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = requireContext().getString(R.string.channel_name)
            val descriptionText = requireContext().getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            Log.i(TAG, "setupNotificationChannel: ")
            val channel =
                NotificationChannel(WeatherNotificationWorker.CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    Array(1) { Manifest.permission.POST_NOTIFICATIONS },
                    101
                );
            }

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:com.omarinc.weather")
            )
            startActivityForResult(intent, 101)
        }

        settingsViewModel.writeStringToSharedPreferences(
            Constants.KEY_NOTIFICATIONS_ENABLED,
            Constants.VALUE_ENABLE
        )

    }

    override fun onDeleteAlertClick(alert: WeatherAlert) {
        viewModel.deleteAlert(alert)
        unregisterAlarm(alert.id)
        unregisterNotification(alert.id)
        Log.i(TAG, "onDeleteAlertClick: ${alert.id}")
        val snackbar =
            Snackbar.make(binding.root, "${alert.locationName} Deleted", Snackbar.LENGTH_LONG)
        snackbar.setAction(getString(R.string.undo)) {


            if (alert.alertTime < System.currentTimeMillis()) {
                viewModel.insertAlert(alert)
            } else {
                viewModel.insertAlert(alert)
                Helpers.registerAlarm(
                    context = requireContext(),
                    alertId = alert.id,
                    lat = alert.latitude,
                    lng = alert.longitude,
                    locationName = alert.locationName,
                    alertTimeMillis = alert.alertTime

                )
                Helpers.registerNotification(
                    context = requireContext(),
                    alertId = alert.id,
                    lat = alert.latitude,
                    lng = alert.longitude,
                    locationName = alert.locationName,
                    timeStamp = alert.alertTime
                )
            }
        }
        snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        snackbar.show()

    }


    private fun unregisterAlarm(alertId: Int) {
        Log.i(TAG, "unregisterAlarm: $alertId")
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alertId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun unregisterNotification(alertId: Int) {
        WorkManager.getInstance(requireContext()).cancelUniqueWork("Notification_$alertId")
    }

    override fun onNotificationClick(alert: WeatherAlert) {
        val currentState = notificationStates.getOrDefault(alert.id, false)
        if (!currentState) {
            Snackbar.make(
                binding.root,
                "${alert.locationName} ${getString(R.string.notification_disabled)}",
                Snackbar.LENGTH_LONG
            ).show()
            unregisterAlarm(alert.id)
            unregisterNotification(alert.id)
        } else {
            Snackbar.make(
                binding.root,
                "${alert.locationName} ${getString(R.string.notification_enabled)}",
                Snackbar.LENGTH_LONG
            ).show()

            Helpers.registerAlarm(
                context = requireContext(),
                alertId = alert.id,
                lat = alert.latitude,
                lng = alert.longitude,
                locationName = alert.locationName,
                alertTimeMillis = alert.alertTime

            )

            Helpers.registerNotification(
                context = requireContext(),
                alertId = alert.id,
                lat = alert.latitude,
                lng = alert.longitude,
                locationName = alert.locationName,
                timeStamp = alert.alertTime
            )

        }


        notificationStates[alert.id] = !currentState

    }


}


