package com.example.tracknscan.viewModel.mapTracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MapViewModelFactory:  ViewModelProvider.Factory  {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel() as T
        }
        throw IllegalArgumentException("Unknown class name")
    }

}