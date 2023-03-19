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
        Log.i("BluetoothViewModel", "startScanning")
        bluetoothController.startDiscovery()
    }

    fun stopScanning() {
        bluetoothController.stopDiscovery()
    }

}