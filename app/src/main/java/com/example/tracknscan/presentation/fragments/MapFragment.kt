package com.example.tracknscan.presentation.fragments

import android.Manifest
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tracknscan.R
import com.example.tracknscan.databinding.FragmentMapBinding
import com.example.tracknscan.helpers.Constants
import com.example.tracknscan.helpers.throwToast
import com.example.tracknscan.services.TrackingService
import com.example.tracknscan.viewModel.mapTracking.MapViewModel
import com.example.tracknscan.viewModel.mapTracking.MapViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MapViewModel

    private val locationManager by lazy {
        requireContext().getSystemService(LocationManager::class.java)
    }

    private val isLocationEnabled: Boolean
        get() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    private var mMap: GoogleMap? = null

    private var allLocationsPoints = mutableListOf<LatLng>()
    private var locationPoint = LatLng(0.0,0.0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        // ViewModelFactory to pass input data to the ViewModel
        viewModel = ViewModelProvider(this, MapViewModelFactory())[MapViewModel::class.java]

        _binding = FragmentMapBinding
            .inflate(inflater, container, false)
            .apply {
                this.vm = viewModel
            }

        askLocationPermission()

        return binding.root
    }

    private fun askLocationPermission() {

        // register for result - checking if user enabled Location
        val enableLocationLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(!isLocationEnabled) {
                locationResultCanceled()
            } else {
                startLocationService()
            }
        }

        // register for result - Permissions(location)
        val permissionLauncher  = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val preciseLocationGranted = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            } else true

            val approximateLocationGranted = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            } else true

            // permission to use location -> granted
            if(preciseLocationGranted || approximateLocationGranted) {
                if(!isLocationEnabled) {
                    // location disabled -> ask user to turn on
                    enableLocationLauncher.launch(
                        Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    )
                    throwToast(requireContext(), "Enable Location to continue.")
                } else {
                    startLocationService()
                }
            } else {
                throwToast(requireContext(), "Location permission denied, try again later.")
            }
        }

        // check whether app already has the permissions
        permissionLauncher .launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))

    }

    private fun startLocationService() {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = Constants.ACTION_START
            requireActivity().startService(it)
        }
        subscribeToObservers()
    }


    private fun subscribeToObservers() {
        TrackingService.allLocationsPoints.observe(viewLifecycleOwner, Observer {
            allLocationsPoints = it
            addAllLocationToMap()
        })
    }

    private fun addAllLocationToMap() {
        for(location in allLocationsPoints) {
            locationPoint = location

            // put marker on map
            val mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }
    }

    private fun locationResultCanceled() {
        // User did not enable Location or an error occurred
        Log.d("Location", "Location not enabled")
        throwToast(requireContext(), "Location not enabled, try again later.")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if(locationPoint.latitude != 0.0 && locationPoint.longitude != 0.0) { // not default
            val markerOptions =
                MarkerOptions().position(locationPoint).title(locationPoint.toString())

            mMap?.animateCamera(CameraUpdateFactory.newLatLng(locationPoint))
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(locationPoint, 20f))
            mMap?.addMarker(markerOptions)
        }
    }

}