package com.example.tracknscan.model.mapTracking.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.example.tracknscan.helpers.hasLocationPermission
import com.example.tracknscan.helpers.throwToast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class LocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
) {

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            if(!context.hasLocationPermission()){
                throwToast(context, "Missing Location permission.")
            }

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if(!isGpsEnabled && !isNetworkEnabled) {
                throwToast(context, "Location is disabled.")
            }

            // request location
            val request = LocationRequest.create()
                .setInterval(interval)
                .setFastestInterval(interval)

            // fetching new location
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let { location ->
                        launch { send(location) }
                    }
                }
            }

            // request
            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )

            // when stop getting location updates
            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }

        }
    }

}