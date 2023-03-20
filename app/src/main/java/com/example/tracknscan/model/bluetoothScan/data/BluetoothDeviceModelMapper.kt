package com.example.tracknscan.model.bluetoothScan.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.tracknscan.model.bluetoothScan.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceModel(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}