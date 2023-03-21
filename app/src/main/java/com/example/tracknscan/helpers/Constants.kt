package com.example.tracknscan.helpers

import com.example.tracknscan.R

object Constants {

    /*********** Bluetooth ***********/
    const val mapIdFragment: Int = R.id.map
    const val bluetoothIdFragment: Int = R.id.bluetooth



    /*********** Map ***********/

    const val locationInterval: Long = 10000L // 1 min

    // Service
    const val ACTION_START = "ACTION_START"
    const val ACTION_STOP = "ACTION_STOP"

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "tracking"
    const val NOTIFICATION_ID = 1

    // DB
    const val db_name = "TRACK and SCAN DB"
    const val db_version = 1
    const val table_name = "lastLocations"
    const val id_column = "id_lat_lon"
    const val lat_column = "latitude"
    const val lon_column = "longitude"


}