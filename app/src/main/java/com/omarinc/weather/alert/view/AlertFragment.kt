package com.omarinc.weather.alert.view

import android.Manifest
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.fragment.findNavController
import com.omarinc.weather.R
import com.omarinc.weather.alert.workmanger.WeatherNotificationWorker
import com.omarinc.weather.databinding.FragmentAlertBinding
import com.omarinc.weather.databinding.FragmentFavoriteBinding
import com.omarinc.weather.utilities.Constants
import java.util.Calendar


class AlertFragment : Fragment() {


    private  lateinit var binding: FragmentAlertBinding
    private  val TAG = "AlertFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.fabAddAlert.setOnClickListener{

            setupNotificationChannel()
            showDateTimePicker()

        }

    }




    fun showDateTimePicker() {
        val currentDate = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth, hourOfDay, minute)
                navigateToLocationSelection(selectedDate.timeInMillis)
            }
            TimePickerDialog(context, timeSetListener, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show()
        }
        DatePickerDialog(requireContext(), dateSetListener, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show()
    }
    fun navigateToLocationSelection(timeInMillis: Long) {
        Log.i(TAG, "navigateToLocationSelection: $timeInMillis")
        val action = AlertFragmentDirections.actionAlertFragmentToMapFragment()
        action.fragmentType= Constants.FRAGMENT_TYPE
        action.timeStamp=timeInMillis
        findNavController().navigate(action)
    }


    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = requireContext().getString(R.string.channel_name)
            val descriptionText = requireContext().getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            Log.i(TAG, "setupNotificationChannel: ")
            val channel = NotificationChannel(WeatherNotificationWorker.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),  Array(1){Manifest.permission.POST_NOTIFICATIONS},101);
            }

        }
    }
}