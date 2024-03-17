package com.omarinc.weather

import android.location.Address
import android.location.Geocoder
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import com.google.android.gms.common.api.Status

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.omarinc.weather.map.LocationBottomSheetFragment
import java.io.IOException
import java.util.Locale

class MapFragment : Fragment(),OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    lateinit var geocoder: Geocoder


    private  val TAG = "MapFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        geocoder = Geocoder(requireContext(), Locale.getDefault())


    }



    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng))
            // Here, latLng is the coordinate where the user tapped
            // You can use latLng.latitude and latLng.longitude
            var addressText:String=""
            var addresses: List<Address>?
            try {
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (addresses != null) {
                    addressText = addresses.firstOrNull()?.subAdminArea+" , "+addresses.firstOrNull()?.countryName ?: getString(R.string.address_not_found)
                }
            } catch (e: IOException) {
                addressText = "Unable to get address"
            }

            val locationBottomSheetFragment = LocationBottomSheetFragment(addressText,latLng.latitude, latLng.longitude)
            locationBottomSheetFragment.show(parentFragmentManager, LocationBottomSheetFragment.TAG)
        }
    }
}