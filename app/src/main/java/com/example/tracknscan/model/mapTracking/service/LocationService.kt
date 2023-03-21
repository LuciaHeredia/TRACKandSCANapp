package com.example.tracknscan.model.mapTracking.service

import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.tracknscan.helpers.Constants
import com.example.tracknscan.model.mapTracking.data.LocationClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// LifecycleService for observe function
class LocationService: LifecycleService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient

    companion object {
        val allLocationsPoints = MutableLiveData<MutableList<LatLng>>()
    }

    private fun postInitialValues() {
        allLocationsPoints.postValue(mutableListOf()) // empty list
    }

    override fun onCreate() {
        super.onCreate()

        postInitialValues()

        locationClient = LocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            Constants.ACTION_START -> start()
            Constants.ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {

        locationClient.getLocationUpdates(Constants.locationInterval)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                val newLocation = LatLng(location.latitude, location.longitude)
                Log.d("service: ", newLocation.toString())
                addLocation(location)
            }
            .launchIn(serviceScope)
    }

    private fun stop() {
        stopSelf()
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