package com.omarinc.weather.favorite.view

import com.omarinc.weather.model.FavoriteCity

interface OnClick {
    fun onDeleteFavoriteClick(city: FavoriteCity )

    fun onCityClick(city: FavoriteCity)
}