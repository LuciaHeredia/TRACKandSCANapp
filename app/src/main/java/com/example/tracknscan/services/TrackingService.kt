package com.example.tracknscan.services

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Looper
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
import java.util.*

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
                    val pos = LocationDomain(getId(), location.latitude, location.longitude)
                    addLocation(pos)
                }
            }
        }
    }

    fun getId(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        val milli = calendar.get(Calendar.MILLISECOND)
        return "TIME(H:M:S:M)= $hour:$minute:$seconds:$milli"
    }

    private fun addLocation(location: LocationDomain?) {
        location?.let {
            val pos = LocationDomain(location.id, location.latitude, location.longitude)

            // db size bounds
            if(allLocationsPoints.value?.size!! >= Constants.Map.DB_SIZE) {
                deleteLocation.value = allLocationsPoints.value!!.first()
                dbHelper.deleteFirstLocation(deleteLocation.value!!) // remove from db
                allLocationsPoints.value?.removeFirst() // remove from list
            }
            allLocationsPoints.value?.apply {
                add(pos)
                allLocationsPoints.postValue(this) // add to list
            }
            dbHelper.addLocation(pos) // add to db

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }


}