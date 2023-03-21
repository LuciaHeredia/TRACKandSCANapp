package com.example.tracknscan.helpers

import com.example.tracknscan.R

object Constants {

    object Bluetooth {
        const val THROW_ENABLE_BLUETOOTH = "Enable bluetooth to continue."
        const val THROW_BLUETOOTH_NOT_ENABLED = "Bluetooth not enabled, try again later."
        const val THROW_BLUETOOTH_SCAN_PERMISSION_DENIED = "Bluetooth Scan permission denied, try again later."
    }

    object Map {
        // Messages
        const val ANNOUNCE_TRACKING = "Tracking Location Every 1min"
        const val ANNOUNCE_NOT_TRACKING = "No Permission/Location Disabled"
        const val ANNOUNCE_NO_LOCATION = "Location not enabled"
        const val ANNOUNCE_NO_PERMISSION = "Location Permission denied"
        const val THROW_LOCATION_PERMISSION_DENIED = "Location Permission denied, try again later."
        const val THROW_ENABLE_LOCATION = "Enable Location to continue."
        const val THROW_LOCATION_NOT_ENABLED = "Location not enabled, try again later."

        // Service
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        const val LOCATION_UPDATE_INTERVAL: Long = 5000L // 5sec, 60000L=1min
        const val FASTEST_LOCATION_INTERVAL: Long = 2000L // 2sec, 30000L=0.5min

        // DB
        const val DB_NAME = "TRACKnSCANdb"
        const val DB_VERSION = 1
        const val TABLE_NAME = "LAST_LOCATIONS"
        const val ID_COLUMN = "ID"
        const val LAT_COLUMN = "LATITUDE"
        const val LON_COLUMN = "LONGITUDE"

        const val db_size = 5 // 20

        // MAP
        const val MARKER_ZOOM = 20f

    }

    object Errors {
        const val UNKNOWN_CLASS_NAME = "Unknown class name"
    }

}