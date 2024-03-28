package com.omarinc.weather.map

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.omarinc.weather.R
import com.omarinc.weather.utilities.Constants
import java.io.IOException
import java.util.Locale

class MapFragment : Fragment(),OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    lateinit var geocoder: Geocoder
    private lateinit var placesClient: PlacesClient

    private lateinit var autoCompleteTextView: AutoCompleteTextView

    private  var fragmentType:String?=""
    private  var timeStamp:Long?=0


    private  val TAG = "MapFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyDRBdBaadZzV8KXVC3Jooj6LSyEopIlsog")
        }
        placesClient = Places.createClient(requireContext())
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
         fragmentType = arguments?.let { MapFragmentArgs.fromBundle(it).fragmentType }
         timeStamp = arguments?.let { MapFragmentArgs.fromBundle(it).timeStamp }

        Log.i(TAG, "onViewCreated: ")

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        geocoder = Geocoder(requireContext(), Locale.getDefault())

        autoCompleteTextView = view.findViewById(R.id.autoCompleteTextView)
        setupAutoCompleteTextView()


    }

    private fun setupAutoCompleteTextView() {
        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
//        autocompleteFragment.setTheme(R.style.CustomAutocompleteTheme)
        autocompleteFragment.view?.setBackgroundColor(resources.getColor(R.color.white))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                place.latLng?.let {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 18f))
                    googleMap.clear()
                    googleMap.addMarker(MarkerOptions().position(place.latLng).title(place.name))



                    if(fragmentType==Constants.FRAGMENT_TYPE_ALERT)
                    {
                        Log.i(TAG, "FRAGMENT_TYPE_ALERT: ")
                        val locationBottomSheetFragment = LocationBottomSheetFragment(place.name,place.latLng.latitude, place.latLng.longitude,fragmentType=Constants.FRAGMENT_TYPE_ALERT,timeStamp=timeStamp!!)
                        locationBottomSheetFragment.show(parentFragmentManager, LocationBottomSheetFragment.TAG)

                    }
                    else if(fragmentType==Constants.FRAGMENT_TYPE_SETTINGS){

                        Log.i(TAG, "FRAGMENT_TYPE_SETTINGS: ")
                        val locationBottomSheetFragment = LocationBottomSheetFragment(place.name,place.latLng.latitude, place.latLng.longitude,fragmentType=Constants.FRAGMENT_TYPE_SETTINGS)
                        locationBottomSheetFragment.show(parentFragmentManager, LocationBottomSheetFragment.TAG)

                    }else{
                        Log.i(TAG, "else: ")
                        val locationBottomSheetFragment = LocationBottomSheetFragment(place.name,place.latLng.latitude, place.latLng.longitude)
                        locationBottomSheetFragment.show(parentFragmentManager, LocationBottomSheetFragment.TAG)
                    }
                }
            }

            override fun onError(status: Status) {
                Log.i("Places", "An error occurred: $status")
            }
        })
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear()
            googleMap.addMarker(MarkerOptions().position(latLng))

            var addressText:String=""
            var addresses: List<Address>?
            try {
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (addresses != null) {
                    addressText = addresses.firstOrNull()?.subAdminArea+" , "+addresses.firstOrNull()?.countryName ?: getString(
                        R.string.address_not_found
                    )
                }
            } catch (e: IOException) {
                addressText = "Unable to get address"
            }

            if(fragmentType==Constants.FRAGMENT_TYPE_ALERT)
            {

                val locationBottomSheetFragment = LocationBottomSheetFragment(addressText,latLng.latitude, latLng.longitude,fragmentType=Constants.FRAGMENT_TYPE_ALERT,timeStamp=timeStamp!!)
                locationBottomSheetFragment.show(parentFragmentManager, LocationBottomSheetFragment.TAG)

            }
            else if(fragmentType==Constants.FRAGMENT_TYPE_SETTINGS){

                Log.i(TAG, "FRAGMENT_TYPE_SETTINGS: ")
                val locationBottomSheetFragment = LocationBottomSheetFragment(addressText,latLng.latitude, latLng.longitude,fragmentType=Constants.FRAGMENT_TYPE_SETTINGS)
                locationBottomSheetFragment.show(parentFragmentManager, LocationBottomSheetFragment.TAG)

            }

            else{

                val locationBottomSheetFragment = LocationBottomSheetFragment(addressText,latLng.latitude, latLng.longitude)
                locationBottomSheetFragment.show(parentFragmentManager, LocationBottomSheetFragment.TAG)
            }
        }
    }
}