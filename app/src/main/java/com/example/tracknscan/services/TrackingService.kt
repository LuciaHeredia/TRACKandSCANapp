package com.example.tracknscan.services

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Looper
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.tracknscan.helpers.Constants
import com.example.tracknscan.helpers.hasLocationPermission
import com.example.tracknscan.helpers.throwToast
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*

// LifecycleService for object observing
class TrackingService: LifecycleService() {

    var isFirstRun = true

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val allLocationsPoints = MutableLiveData<MutableList<LatLng>>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        allLocationsPoints.postValue(mutableListOf()) // empty list
    }

    override fun onCreate() {
        super.onCreate()

        postInitialValues()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            Constants.ACTION_START ->
                if(isFirstRun) {
                    start()
                    isFirstRun = false
                }
            Constants.ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        isTracking.postValue(true)
    }

    private fun stop() {
        stopSelf()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(tracking: Boolean) {
        if(tracking) {
            if(applicationContext.hasLocationPermission()) {
                // request location
                val request = LocationRequest().apply {
                    interval = Constants.LOCATION_UPDATE_INTERVAL
                    fastestInterval = Constants.FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                throwToast(applicationContext, "Missing Location permission.")
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    // fetching new location
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if(isTracking.value!!) {
                result.locations.lastOrNull()?.let { location ->
                    addLocation(location)
                }
            }
        }
    }

    private fun addLocation(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            allLocationsPoints.value?.apply {
                add(pos)
                allLocationsPoints.postValue(this)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }


}