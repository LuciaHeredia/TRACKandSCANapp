package com.example.tracknscan.helpers

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat


@RequiresApi(Build.VERSION_CODES.S)
fun Context.hasBluetoothPermission(): Boolean {
    val bluetoothManager = applicationContext.getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter = bluetoothManager?.adapter
    return  bluetoothAdapter?.isEnabled == true
}

fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}

fun Context.isLocationEnabled(): Boolean {
    val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    return isGpsEnabled && isNetworkEnabled
}