package com.example.tracknscan.viewModel.bluetoothScan

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tracknscan.data.bluetoothScan.BluetoothController

class BluetoothViewModel(
    private val bluetoothController: BluetoothController
): ViewModel() {

    // current devices scanned
    val devicesToShow = bluetoothController.scannedDevices

    fun startScanning() {
        bluetoothController.startDiscovery()
    }

    fun stopScanning() {
        bluetoothController.stopDiscovery()
    }

    fun filterList(address: String) {
        bluetoothController.filterListByAddress(address)
    }

    fun releaseDataReceiver(){
        bluetoothController.release()
    }

}