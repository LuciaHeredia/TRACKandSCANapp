package com.example.tracknscan.viewModel.mapTracking

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tracknscan.model.mapTracking.data.MapController

class MapViewModel(
    private val mapController: MapController
): ViewModel() {

    // current locations list
    val locationsToShow = mapController.locations

    fun getCurrentLocationUser() {
        Log.i("MapViewModel", "getCurrentLocationUser()")
        mapController.getLocationUpdates(1000)
    }

}