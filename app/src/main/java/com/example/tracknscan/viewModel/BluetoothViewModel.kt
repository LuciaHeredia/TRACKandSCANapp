package com.example.tracknscan.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tracknscan.model.bluetoothScan.data.AndroidBluetoothController

class BluetoothViewModel(
    private val bluetoothController: AndroidBluetoothController
): ViewModel() {

    // current devices scanned
    val state = bluetoothController.scannedDevices

    fun startScanning() {
        bluetoothController.startDiscovery()
    }

    fun stopScanning() {
        bluetoothController.stopDiscovery()
    }

    fun filterList(address: String) {
        Log.i("BluetoothViewModel", "filterList")
        bluetoothController.filterListByAddress(address)
    }

    fun releaseDataReceiver(){
        bluetoothController.release()
    }

}