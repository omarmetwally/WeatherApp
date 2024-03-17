package com.omarinc.weather.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.omarinc.weather.R
import com.omarinc.weather.databinding.FragmentFavoriteBinding
import com.omarinc.weather.databinding.FragmentLocationBottomSheetBinding

class LocationBottomSheetFragment(val location:String,val lat: Double, val lng: Double) : BottomSheetDialogFragment() {

    lateinit var binding:FragmentLocationBottomSheetBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLocationBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvLocationName.text="Location: $location"
        binding.tvLat.text = "Latitude: $lat"
        binding.tvLng.text = "Longitude: $lng"

        binding.btnSaveLocation.setOnClickListener {
            // Implement save functionality
            dismiss()
        }
    }

    companion object {
        const val TAG = "LocationBottomSheetFragment"
    }
}
