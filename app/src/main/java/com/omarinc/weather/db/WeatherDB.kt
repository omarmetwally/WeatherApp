package com.omarinc.weather.db
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.WeatherAlert
import com.omarinc.weather.model.WeatherResponse


@Database(entities = [FavoriteCity::class, WeatherResponse::class,WeatherAlert::class], version = 4)
@TypeConverters(Converter::class)
abstract class WeatherDB: RoomDatabase() {

    abstract fun getDao(): FavouriteDao
    abstract fun getAlertDao(): WeatherAlertDao
    abstract fun getWeatherDao(): WeatherDao

    companion object {
        private var instance: WeatherDB? = null

        fun getInstance(context: Context): WeatherDB {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDB::class.java,
                    "app_db"
                ).build().also {
                    instance = it
                }
            }
        }
    }
}