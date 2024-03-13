package com.omarinc.weather.currentHomeWeather.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.omarinc.weather.R
import com.omarinc.weather.databinding.ItemDaysBinding
import com.omarinc.weather.model.DailyForecastUI
import com.omarinc.weather.model.DailyWeather

class MyDailyAdapter : ListAdapter<DailyForecastUI, MyDailyAdapter.ViewHolder>(
    DailyForecastUIDiffUtil()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDaysBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentWeather = getItem(position)

        with(holder.binding) {
            tvDayDays.text = currentWeather.date
            tvStatusDays.text = currentWeather.condition
            setIcon(currentWeather.icon, ivIconDays)
            tvDegreeDays.text = String.format("%s°C", currentWeather.averageTemp.toString())
        }
    }

    inner class ViewHolder(val binding: ItemDaysBinding) : RecyclerView.ViewHolder(binding.root)
}

class DailyForecastUIDiffUtil : DiffUtil.ItemCallback<DailyForecastUI>() {
    override fun areItemsTheSame(oldItem: DailyForecastUI, newItem: DailyForecastUI): Boolean {
        return oldItem.date == newItem.date
    }

    override fun areContentsTheSame(oldItem: DailyForecastUI, newItem: DailyForecastUI): Boolean {
        return oldItem == newItem
    }
}
fun setIcon(id: String, iv: ImageView){
    when (id) {
        "01d" -> iv.setImageResource(R.drawable.sun)
        "02d" -> iv.setImageResource(R.drawable._02d)
        "03d" -> iv.setImageResource(R.drawable._03d)
        "04d" -> iv.setImageResource(R.drawable._04n)
        "09d" -> iv.setImageResource(R.drawable._09n)
        "10d" -> iv.setImageResource(R.drawable._10d)
        "11d" -> iv.setImageResource(R.drawable._11d)
        "13d" -> iv.setImageResource(R.drawable._13d)
        "50d" -> iv.setImageResource(R.drawable._50d)
        "01n" -> iv.setImageResource(R.drawable._01n)
        "02n" -> iv.setImageResource(R.drawable._02n)
        "03n" -> iv.setImageResource(R.drawable._03d)
        "04n" -> iv.setImageResource(R.drawable._04n)
        "09n" -> iv.setImageResource(R.drawable._09n)
        "10n" -> iv.setImageResource(R.drawable._10n)
        "11n" -> iv.setImageResource(R.drawable._11d)
        "13n" -> iv.setImageResource(R.drawable._13d)
        "50n" -> iv.setImageResource(R.drawable._50d)
        else -> iv.setImageResource(R.drawable._load)
    }
}