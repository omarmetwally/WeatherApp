package com.omarinc.weather.alert.view

import com.omarinc.weather.model.WeatherAlert

interface OnNotificationClick {

    fun onNotificationClick(alert: WeatherAlert)

}