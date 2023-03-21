package com.example.tracknscan.ui.fragments

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tracknscan.R
import com.example.tracknscan.databinding.FragmentMapBinding
import com.example.tracknscan.helpers.Constants
import com.example.tracknscan.helpers.isLocationEnabled
import com.example.tracknscan.helpers.throwToast
import com.example.tracknscan.model.locationTrack.LocationDomain
import com.example.tracknscan.services.TrackingService
import com.example.tracknscan.viewModel.locationTrack.MapViewModel
import com.example.tracknscan.viewModel.locationTrack.MapViewModelFactory
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

    private var spinner: ProgressBar? = null
    private var mMap: GoogleMap? = null

    private var isTracking = false
    private var allLocationsPoints = mutableListOf<LocationDomain>()
    private var deleteMarker = LocationDomain("0.0",0.0,0.0)

    private var firstMarkersInit = true
    private var markerList:ArrayList<MarkerOptions> = ArrayList()

    private var addNewMarker = false
    private var globalLocation = LatLng(0.0,0.0)
    private var globalMarkerOptions =
        MarkerOptions().position(globalLocation).title("")

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

        // init progress bar
        spinner = binding.mProgressBar
        spinner!!.visibility = View.GONE

        subscribeToObservers()

        if(!isTracking) {
            askLocationPermission()
        }

        return binding.root
    }

    private fun askLocationPermission() {
        spinner!!.visibility = View.GONE

        // register for result - checking if user enabled Location
        val enableLocationLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(!requireContext().isLocationEnabled()) {
                binding.mDescText.text = Constants.Map.ANNOUNCE_NO_LOCATION
            } else {
                binding.mDescText.text = Constants.Map.ANNOUNCE_TRACKING
                spinner?.visibility = View.VISIBLE // show progress bar
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
                if(!requireContext().isLocationEnabled()) {
                    // location disabled -> ask user to turn on
                    enableLocationLauncher.launch(
                        Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    )
                    throwToast(requireContext(), Constants.Map.THROW_ENABLE_LOCATION)
                } else {
                    binding.mDescText.text = Constants.Map.ANNOUNCE_TRACKING
                    spinner?.visibility = View.VISIBLE // show progress bar
                    startLocationService()
                }
            } else {
                binding.mDescText.text = Constants.Map.ANNOUNCE_NO_PERMISSION
            }
        }

        // check whether app already has the permissions
        permissionLauncher .launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))

    }

    private fun startLocationService() {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = Constants.Map.ACTION_START
            requireActivity().startService(it)
        }
    }

    private fun subscribeToObservers() {

        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.allLocationsPoints.observe(viewLifecycleOwner, Observer {
            this.allLocationsPoints = it // update list

            // init markers
            if(firstMarkersInit){
                initMapMarkers()
            }

            // add new marker
            if(allLocationsPoints.isNotEmpty()) {
                addNewLocationToMap(allLocationsPoints.last())
            }

            // map
            val mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        })

        TrackingService.deleteLocation.observe(viewLifecycleOwner, Observer {
            deleteMarker = it

            //mMap?.clear()
            //addAllAtOnce()
        })
    }

    private fun initMapMarkers() {
        for(location in allLocationsPoints) {
            val loc = LatLng(location.latitude, location.longitude)
            val markerOptions =
                MarkerOptions().position(loc).title(location.id)
            markerList.add(markerOptions)
        }
    }

    private fun addNewLocationToMap(lastLocation: LocationDomain) {
        addNewMarker = true
        globalLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
        globalMarkerOptions = MarkerOptions().position(globalLocation).title(lastLocation.id)
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking

        if(!isTracking) {
            binding.mDescText.text = Constants.Map.ANNOUNCE_NOT_TRACKING
        } else{
            binding.mDescText.text = Constants.Map.ANNOUNCE_TRACKING
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if(firstMarkersInit) {
            for (marker in markerList) {
                val location: LatLng = marker.position
                val markerOptions: MarkerOptions = marker
                addMarker(location, markerOptions)
            }
            firstMarkersInit = false
        }

        if(addNewMarker) {
            addMarker(globalLocation, globalMarkerOptions)
            addNewMarker = false
        }

    }

    private fun addMarker(location: LatLng, markerOptions: MarkerOptions) {
        mMap?.animateCamera(CameraUpdateFactory.newLatLng(location))
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, Constants.Map.MARKER_ZOOM))
        mMap?.addMarker(markerOptions)
    }

}