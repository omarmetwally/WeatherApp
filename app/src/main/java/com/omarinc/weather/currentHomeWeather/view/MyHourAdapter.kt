package com.omarinc.weather.currentHomeWeather.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.weather.databinding.ItemHoursBinding
import com.omarinc.weather.model.TodayForecast

class MyHourAdapter : ListAdapter<TodayForecast, MyHourAdapter.ViewHolder>(
    TodayForecastUIDiffUtil()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHoursBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentForecast = getItem(position)

        with(holder.binding) {
            tvTimeHours.text = currentForecast.time
            tvDegreeHours.text = String.format("%s°C", currentForecast.temp.toString())
            setIcon(currentForecast.icon, ivStatusIconHours)
        }
    }
    inner class ViewHolder(val binding: ItemHoursBinding) : RecyclerView.ViewHolder(binding.root)
}

class TodayForecastUIDiffUtil : DiffUtil.ItemCallback<TodayForecast>() {
    override fun areItemsTheSame(oldItem: TodayForecast, newItem: TodayForecast): Boolean {
        return oldItem.time == newItem.time
    }

    override fun areContentsTheSame(oldItem: TodayForecast, newItem: TodayForecast): Boolean {
        return oldItem == newItem
    }
}