package com.omarinc.weather.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.omarinc.weather.R
import com.omarinc.weather.currentHomeWeather.viewmodel.HomeViewModel
import com.omarinc.weather.currentHomeWeather.viewmodel.ViewModelFactory
import com.omarinc.weather.databinding.FragmentFavoriteBinding
import com.omarinc.weather.databinding.FragmentLocationBottomSheetBinding
import com.omarinc.weather.db.WeatherLocalDataSourceImpl
import com.omarinc.weather.favorite.viewmodel.FavoriteViewModel
import com.omarinc.weather.model.FavoriteCity
import com.omarinc.weather.model.WeatherRepositoryImpl
import com.omarinc.weather.network.WeatherRemoteDataSourceImpl
import com.omarinc.weather.sharedpreferences.SharedPreferencesImpl

class LocationBottomSheetFragment(val location:String,val lat: Double, val lng: Double) : BottomSheetDialogFragment() {

    lateinit var binding:FragmentLocationBottomSheetBinding
    private lateinit var viewModel: FavoriteViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLocationBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = ViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                WeatherRemoteDataSourceImpl.getInstance(requireContext()),
                WeatherLocalDataSourceImpl.getInstance(requireContext()),
                SharedPreferencesImpl.getInstance(requireContext())

            ),
            requireActivity()
        )
        viewModel = ViewModelProvider(requireActivity(), factory).get(FavoriteViewModel::class.java)


        binding.tvLocationName.text="${getString(R.string.location)}  $location"
        binding.tvLat.text = "${getString(R.string.Latitude)}  $lat"
        binding.tvLng.text = "${getString(R.string.Longitude)} $lng"

        binding.btnSaveLocation.setOnClickListener {
            viewModel.insertFavorite(FavoriteCity(cityName = location, latitude = lat, longitude = lng))
            findNavController().navigateUp()
            dismiss()
        }
    }

    companion object {
        const val TAG = "LocationBottomSheetFragment"
    }
}
