package com.example.tracknscan.model.locationTrack

data class LocationDomain(
    val id: String, // unique mac address for each device
    val latitude: Double,
    val longitude: Double
)
