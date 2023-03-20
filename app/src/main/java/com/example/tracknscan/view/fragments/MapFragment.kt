package com.example.tracknscan.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.tracknscan.R
import com.example.tracknscan.databinding.FragmentMapBinding
import com.example.tracknscan.helpers.hasLocationPermission
import com.example.tracknscan.helpers.throwToast
import com.example.tracknscan.model.mapTracking.LocationClient
import com.example.tracknscan.model.mapTracking.data.MapController
import com.example.tracknscan.viewModel.mapTracking.MapViewModel
import com.example.tracknscan.viewModel.mapTracking.MapViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        // location service
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // ViewModelFactory to pass input data to the ViewModel
        viewModel = ViewModelProvider(this, MapViewModelFactory(MapController(requireContext(), fusedLocationProviderClient)))[MapViewModel::class.java]

        _binding = FragmentMapBinding
            .inflate(inflater, container, false)
            .apply {
                this.vm = viewModel
            }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        askLocationPermission()
        observeLocationsList()

        return binding.root
    }

    private fun askLocationPermission() {

        // register for result - checking if user enabled Location
        val enableLocationLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(!isLocationEnabled) {
                locationResultCanceled()
            } else {
                viewModel.getCurrentLocationUser()
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
                    throwToast(requireContext(), "Enable location to continue.")
                } else {
                    viewModel.getCurrentLocationUser()
                }
            } else {
                throwToast(requireContext(), "Location permission denied, try again later.",)
            }
        }

        // check whether app already has the permissions
        permissionLauncher .launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))

    }

    private fun observeLocationsList() {

        viewModel.locationsToShow.observe(viewLifecycleOwner
        ) { locations ->
            if(locations.isNotEmpty()) {
                Log.d("LOCATIONS RECEIVED", locations.last().toString())
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // add marker on map
        // val latLong = LatLng(currentLocation.latitude, currentLocation.longitude)
        // val markerOptions = MarkerOptions().position(latLong).title("current Location")

        //  mMap.animateCamera(CameraUpdateFactory.newLatLng(latLong))
        //  mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, 7f))
        // mMap.addMarker(markerOptions)

    }

    private fun locationResultCanceled() {
        // User did not enable Location or an error occurred
        Log.d("Location", "Location not enabled")
        throwToast(requireContext(), "Location not enabled, try again later.",)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}