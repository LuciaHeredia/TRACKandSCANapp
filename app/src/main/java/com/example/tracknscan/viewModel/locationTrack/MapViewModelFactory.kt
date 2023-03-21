package com.example.tracknscan.viewModel.locationTrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tracknscan.helpers.Constants

class MapViewModelFactory:  ViewModelProvider.Factory  {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel() as T
        }
        throw IllegalArgumentException(Constants.Errors.UNKNOWN_CLASS_NAME)
    }

}