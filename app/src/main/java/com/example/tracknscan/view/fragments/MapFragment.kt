package com.example.tracknscan.view.fragments

import android.Manifest
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.tracknscan.databinding.FragmentMapBinding
import com.example.tracknscan.helpers.throwToast
import com.example.tracknscan.model.mapTracking.data.MapController
import com.example.tracknscan.viewModel.mapTracking.MapViewModel
import com.example.tracknscan.viewModel.mapTracking.MapViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
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
    ): View? {

        // location service
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // ViewModelFactory to pass input data to the ViewModel
        viewModel = ViewModelProvider(this, MapViewModelFactory(MapController(requireContext(), fusedLocationProviderClient)))[MapViewModel::class.java]

        _binding = FragmentMapBinding
            .inflate(inflater, container, false)
            .apply {
                this.vm = viewModel
            }

        /*val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)*/

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
         val permissionLauncher = registerForActivityResult(
             ActivityResultContracts.RequestMultiplePermissions()
         ) {
             if(!isLocationEnabled) {
                 // location disabled -> ask user to turn on
                 enableLocationLauncher.launch(
                     Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                 )
                 throwToast(requireContext(), "Enable location to continue.")
             } else {
                 viewModel.getCurrentLocationUser()
             }
         }

        // LOCATION capability's ON
        permissionLauncher.launch(
             arrayOf(
                 Manifest.permission.ACCESS_COARSE_LOCATION,
                 Manifest.permission.ACCESS_FINE_LOCATION
             )
         )
    }

    private fun observeLocationsList() {
        viewModel.locationsToShow.observe(viewLifecycleOwner
        ) { locations ->
            Log.d("Map", locations.listIterator().toString())
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(
            MarkerOptions()
            .position(sydney)
            .title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun locationResultCanceled() {
        // User did not enable Location or an error occurred
        Log.d("Location", "Location not enabled")
        throwToast(requireContext(), "Location not enabled, try again later.",)
    }

}