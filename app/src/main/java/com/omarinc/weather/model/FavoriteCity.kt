package com.omarinc.weather.model
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "favoriteCity")
data class FavoriteCity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var cityName: String,
    var latitude: Double,
    var longitude: Double
): Serializable