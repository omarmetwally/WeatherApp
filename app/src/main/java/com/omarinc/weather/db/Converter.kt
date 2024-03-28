package com.omarinc.weather.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.omarinc.weather.model.ForecastEntry
import com.omarinc.weather.model.CityInfo

class Converter {
    private val gson = Gson()

    @TypeConverter
    fun fromForecastEntryList(list: List<ForecastEntry>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toForecastEntryList(data: String?): List<ForecastEntry>? {
        if (data == null) return null
        val listType = object : TypeToken<List<ForecastEntry>>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun fromCityInfo(cityInfo: CityInfo?): String? {
        return gson.toJson(cityInfo)
    }

    @TypeConverter
    fun toCityInfo(data: String?): CityInfo? {
        return gson.fromJson(data, CityInfo::class.java)
    }
}
