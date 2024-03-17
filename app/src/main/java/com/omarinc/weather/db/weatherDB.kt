package com.omarinc.weather.db
import android.content.Context
import androidx.databinding.adapters.Converters
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.omarinc.weather.model.WeatherResponse
import com.omarinc.weather.model.FavoriteCity

//, WeatherResponse::class
@Database(entities = [FavoriteCity::class], version = 1)
//@TypeConverters(Converters::class)
abstract class weatherDB: RoomDatabase() {

    abstract fun getDao(): FavouriteDao

    companion object {
        private var instance: weatherDB? = null

        fun getInstance(context: Context): weatherDB {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    weatherDB::class.java,
                    "favorite_db"
                ).build().also {
                    instance = it
                }
            }
        }
    }
}