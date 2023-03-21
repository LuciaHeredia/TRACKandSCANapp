package com.example.tracknscan.viewModel.bluetoothScan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tracknscan.data.bluetoothScan.BluetoothController
import com.example.tracknscan.helpers.Constants

class BluetoothViewModelFactory(
    private val bluetoothController: BluetoothController
):  ViewModelProvider.Factory  {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BluetoothViewModel::class.java)) {
            return BluetoothViewModel(bluetoothController) as T
        }
        throw IllegalArgumentException(Constants.Errors.UNKNOWN_CLASS_NAME)
    }

}