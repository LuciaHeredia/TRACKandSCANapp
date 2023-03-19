package com.example.tracknscan.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tracknscan.model.bluetoothScan.data.AndroidBluetoothController

class BluetoothViewModelFactory(
    private val bluetoothController: AndroidBluetoothController
):  ViewModelProvider.Factory  {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BluetoothViewModel::class.java)) {
            return BluetoothViewModel(bluetoothController) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }

}