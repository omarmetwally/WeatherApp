package com.omarinc.weather.favorite.view



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.omarinc.weather.databinding.ItemFavoriteBinding
import com.omarinc.weather.databinding.ItemHoursBinding
import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.TodayForecast

class FavoriteAdapter(private val onDeleteClick:(city:FavoriteCity)->Unit,private val onCityClick:(city:FavoriteCity)->Unit) : ListAdapter<FavoriteCity, FavoriteAdapter.ViewHolder>(
    FavoriteDiffUtil()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentFavoriteCity = getItem(position)

        with(holder.binding) {
            tvCity.text = currentFavoriteCity.cityName
            ivIconDelete.setOnClickListener{
                onDeleteClick(currentFavoriteCity)
            }

        }
        holder.binding.linearLayoutFav.setOnClickListener{
            onCityClick(currentFavoriteCity)
        }
    }
    inner class ViewHolder(val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root)
}

class FavoriteDiffUtil : DiffUtil.ItemCallback<FavoriteCity>() {
    override fun areItemsTheSame(oldItem: FavoriteCity, newItem: FavoriteCity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FavoriteCity, newItem: FavoriteCity): Boolean {
        return oldItem == newItem
    }
}