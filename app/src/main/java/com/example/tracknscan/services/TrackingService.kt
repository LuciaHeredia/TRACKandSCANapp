package com.example.tracknscan.services

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.tracknscan.data.locationTrack.LocationDbHelper
import com.example.tracknscan.helpers.Constants
import com.example.tracknscan.helpers.hasLocationPermission
import com.example.tracknscan.helpers.isLocationEnabled
import com.example.tracknscan.model.locationTrack.LocationDomain
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import kotlinx.coroutines.*

// LifecycleService for object observing
class TrackingService: LifecycleService() {

    private var isFirstRun = true

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val dbHelper = LocationDbHelper(this, null)


    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val allLocationsPoints = MutableLiveData<MutableList<LocationDomain>>()
        var deleteLocation = MutableLiveData<LocationDomain>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        allLocationsPoints.postValue(dbHelper.getLocations()) // empty list
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
            Constants.Map.ACTION_START ->
                if(isFirstRun) {
                    startService()
                    isFirstRun = false
                }
            Constants.Map.ACTION_PAUSE -> pauseService()
            Constants.Map.ACTION_STOP -> stopService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startService() {
        isTracking.postValue(true)
    }

    private fun pauseService() {
        isTracking.postValue(false)
    }

    private fun stopService() {
        isTracking.postValue(false)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(tracking: Boolean) {
        if(tracking) {
            if(applicationContext.hasLocationPermission()) {
                if(applicationContext.isLocationEnabled()) {
                    // request location
                    val request = LocationRequest().apply {
                        interval = Constants.Map.LOCATION_UPDATE_INTERVAL
                        fastestInterval = Constants.Map.FASTEST_LOCATION_INTERVAL
                        priority = PRIORITY_HIGH_ACCURACY
                    }
                    fusedLocationProviderClient.requestLocationUpdates(
                        request,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                } else {
                    pauseService()
                }
            } else {
                pauseService()
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
                    val id: String = location.latitude.toString()+","+location.longitude.toString()
                    val pos = LocationDomain(id.filter { it.isDigit() }, location.latitude, location.longitude)
                    addLocation(pos)
                }
            }
        }
    }

    private fun addLocation(location: LocationDomain?) {
        location?.let {
            val pos = LocationDomain(location.id, location.latitude, location.longitude)
            Log.d("db SIZE: ", (allLocationsPoints.value?.size?.plus(1)).toString())
            // markers and db size bounds
            /*if(allLocationsPoints.value?.size!! >= Constants.db_size-1) {
                Log.d("Locationnn", allLocationsPoints.value!![0].toString())
                deleteLocation.value = allLocationsPoints.value!![0]
                dbHelper.deleteFirstLocation(deleteLocation.value!!)
                allLocationsPoints.value?.removeFirst()
            } else {*/
                allLocationsPoints.value?.apply {
                    add(pos)
                    allLocationsPoints.postValue(this)
               // }
                dbHelper.addLocation(pos)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }


}