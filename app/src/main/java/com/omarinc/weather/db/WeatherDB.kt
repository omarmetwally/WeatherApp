package com.omarinc.weather.db
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.omarinc.weather.model.FavoriteCity

//, WeatherResponse::class
@Database(entities = [FavoriteCity::class], version = 1)
//@TypeConverters(Converters::class)
abstract class WeatherDB: RoomDatabase() {

    abstract fun getDao(): FavouriteDao

    companion object {
        private var instance: WeatherDB? = null

        fun getInstance(context: Context): WeatherDB {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WeatherDB::class.java,
                    "favorite_db"
                ).build().also {
                    instance = it
                }
            }
        }
    }
}