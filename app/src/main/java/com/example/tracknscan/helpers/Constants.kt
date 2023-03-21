package com.example.tracknscan.helpers

object Constants {

    object Bluetooth {
        const val ANNOUNCE_SCANNING = "Scanning For Bluetooth Devices"
        const val ANNOUNCE_NO_BLUETOOTH = "Bluetooth not enabled"
        const val ANNOUNCE_NO_PERMISSION = "Bluetooth Permission denied"
        const val THROW_ENABLE_BLUETOOTH = "Enable bluetooth to continue."
    }

    object Map {
        // Messages
        const val ANNOUNCE_TRACKING = "Tracking Location Every 1min"
        const val ANNOUNCE_NOT_TRACKING = "No Permission/Location Disabled"
        const val ANNOUNCE_NO_LOCATION = "Location not enabled"
        const val ANNOUNCE_NO_PERMISSION = "Location Permission denied"
        const val THROW_ENABLE_LOCATION = "Enable Location to continue."

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

        const val DB_SIZE = 5 // 20

        // MAP
        const val MARKER_ZOOM = 20f

    }

    object Errors {
        const val UNKNOWN_CLASS_NAME = "Unknown class name"
    }

}