package com.example.tracknscan.data.bluetoothScan

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tracknscan.model.bluetoothScan.BluetoothDeviceDomain
import com.example.tracknscan.model.bluetoothScan.data.toBluetoothDeviceModel

@SuppressLint("MissingPermission")
class BluetoothController(
    private val context: Context
    ){

    var discoveryStarted: Boolean = false

    // to get device's data
    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    // MutableLiveData - value of the data stored within it can be changed
    private val _scannedDevices = MutableLiveData<List<BluetoothDeviceDomain>>(emptyList())
    // wrap data with LiveData
    val scannedDevices: LiveData<List<BluetoothDeviceDomain>>
        get() = _scannedDevices

    var scannedDevicesSaved: List<BluetoothDeviceDomain> = emptyList()

    // put the new device scanned in the Mutable LiveData variable
    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        val newDevice = device.toBluetoothDeviceModel()

        if (!scannedDevicesSaved.contains(newDevice)) {
            scannedDevicesSaved = scannedDevicesSaved + newDevice
        } else {
            _scannedDevices.value = scannedDevicesSaved
        }
    }


    fun startDiscovery() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        discoveryStarted = true

        // registration for device scan result
        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        bluetoothAdapter?.startDiscovery() // by a broadcast receiver: FoundDeviceReceiver
    }

    fun stopDiscovery() {
        if(!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }
        bluetoothAdapter?.cancelDiscovery() // stop scanning for devices
    }

    fun filterListByAddress(address: String) {
        if(address.isNotEmpty()){
            stopDiscovery()
            _scannedDevices.value = scannedDevicesSaved.filter {
                (it.address.lowercase()).contains(address.lowercase())
            }
        } else {
            startDiscovery()
        }
    }

    fun release() {
        // stop receiving updates of scan
        if(discoveryStarted)
            context.unregisterReceiver(foundDeviceReceiver)
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

}