package com.omarinc.weather.currentHomeWeather.viewmodel

import com.omarinc.weather.model.Coordinates

interface LocationUpdateListener {
    fun onLocationUpdated(coordinate: Coordinates)

}