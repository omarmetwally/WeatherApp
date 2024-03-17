package com.omarinc.weather.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omarinc.weather.model.FavoriteCity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteCity(city: FavoriteCity)

    @Delete
    suspend fun deleteFavoriteCity(city: FavoriteCity)

    @Query("SELECT * FROM favoriteCity")
    fun getAllFavouriteCites(): Flow<List<FavoriteCity>>
}