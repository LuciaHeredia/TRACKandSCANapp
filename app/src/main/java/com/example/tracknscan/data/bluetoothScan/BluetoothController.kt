package com.example.tracknscan.data.bluetoothScan

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tracknscan.model.bluetoothScan.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
class BluetoothController(
    private val context: Context
    ){

    private var discoveryStarted: Boolean = false

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

    private var scannedDevicesSaved: List<BluetoothDeviceDomain> = emptyList()

    // put the new device scanned in the Mutable LiveData variable
    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        val newDevice = device.toBluetoothDeviceModel()

        if (!scannedDevicesSaved.contains(newDevice)) {
            scannedDevicesSaved = scannedDevicesSaved + newDevice
        } else {
            _scannedDevices.value = scannedDevicesSaved
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun startDiscovery() {
        if(!hasBluetoothPermission()) {
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

    @RequiresApi(Build.VERSION_CODES.S)
    fun stopDiscovery() {
        if(!hasBluetoothPermission()) {
            return
        }
        bluetoothAdapter?.cancelDiscovery() // stop scanning for devices
    }

    @RequiresApi(Build.VERSION_CODES.S)
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

    @RequiresApi(Build.VERSION_CODES.S)
    private fun hasBluetoothPermission(): Boolean {
        return context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
    }

}