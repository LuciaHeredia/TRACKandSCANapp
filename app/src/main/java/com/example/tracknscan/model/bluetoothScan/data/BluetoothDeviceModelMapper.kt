package com.example.tracknscan.model.bluetoothScan.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.tracknscan.model.bluetoothScan.BluetoothDeviceModel

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceModel(): BluetoothDeviceModel {
    return BluetoothDeviceModel(
        name = name,
        address = address
    )
}