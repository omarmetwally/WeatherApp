package com.omarinc.weather.alert.view

import com.omarinc.weather.currentHomeWeather.view.setIcon

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.weather.R
import com.omarinc.weather.databinding.ItemAlertBinding
import com.omarinc.weather.model.WeatherAlert
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlertAdapter : ListAdapter<WeatherAlert, AlertAdapter.ViewHolder>(
    AlertDiffUtil()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentAlert = getItem(position)
        val animation =
            AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_in_animation)
        holder.itemView.startAnimation(animation)
        with(holder.binding) {
            tvDateTime.text = formatTimestamp(currentAlert.alertTime)
            tvCity.text = currentAlert.locationName

        }
    }

    inner class ViewHolder(val binding: ItemAlertBinding) : RecyclerView.ViewHolder(binding.root)
}

class AlertDiffUtil : DiffUtil.ItemCallback<WeatherAlert>() {
    override fun areItemsTheSame(oldItem: WeatherAlert, newItem: WeatherAlert): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WeatherAlert, newItem: WeatherAlert): Boolean {
        return oldItem == newItem
    }
}



fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("h:mm a, dd MMM-yy", Locale.getDefault())
    return format.format(date)
}