package com.omarinc.weather.alert.view

import com.omarinc.weather.model.WeatherAlert

interface OnAlertClick {

    fun onDeleteAlertClick(alert: WeatherAlert)

}