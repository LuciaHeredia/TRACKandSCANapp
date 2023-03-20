package com.example.tracknscan.viewModel.mapTracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tracknscan.model.mapTracking.data.MapController

class MapViewModelFactory(
    private val mapController: MapController
):  ViewModelProvider.Factory  {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(mapController) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }

}