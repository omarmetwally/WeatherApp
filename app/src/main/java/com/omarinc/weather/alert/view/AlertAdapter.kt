package com.omarinc.weather.alert.view


import android.view.LayoutInflater
import android.view.View
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

class AlertAdapter (private val onDeleteClick:(alert: WeatherAlert)->Unit,private val onNotificationClick: (notification:WeatherAlert)->Unit) : ListAdapter<WeatherAlert, AlertAdapter.ViewHolder>(
    AlertDiffUtil()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var flag:Boolean=false
        val currentAlert = getItem(position)
        val animation =
            AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_in_animation)
        holder.itemView.startAnimation(animation)
        with(holder.binding) {
            tvDateTime.text = formatTimestamp(currentAlert.alertTime)
            tvCity.text = currentAlert.locationName
            ivIconDelete.setOnClickListener{
                onDeleteClick(currentAlert)
            }
            if (currentAlert.alertTime < System.currentTimeMillis()) {
                tvDateTime.paintFlags = tvDateTime.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                tvDateTime.setTextColor(holder.itemView.context.getColor(R.color.red))
                tvCity.paintFlags = tvDateTime.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                tvCity.setTextColor(holder.itemView.context.getColor(R.color.red))
                ivIconNotification.visibility= View.GONE
            }


            ivIconNotification.setOnClickListener{


                if(!flag){
                    ivIconNotification.setImageResource(R.drawable.ic_alert_off_notification)
                    onNotificationClick(currentAlert)
                    flag=true
                }
                else{
                    ivIconNotification.setImageResource(R.drawable.ic_alert_notification)
                    onNotificationClick(currentAlert)
                    flag=false


                }
            }



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