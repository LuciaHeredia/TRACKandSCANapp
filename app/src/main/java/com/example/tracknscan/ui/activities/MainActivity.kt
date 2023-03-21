package com.example.tracknscan.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.tracknscan.R
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private val menu by lazy { findViewById<ChipNavigationBar>(R.id.bottom_menu_bar) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize: Navigation + Bottom Menu
        initNavigationAndMenu()
    }

    private fun initNavigationAndMenu() {
        // Navigation
        val navigationHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navigationHost.navController

        // Bottom Menu
        menu.setItemSelected(R.id.map,true) // init tapped item
        menu.setOnItemSelectedListener(object : ChipNavigationBar.OnItemSelectedListener {
            override fun onItemSelected(id: Int) {
                when (id) {
                    R.id.map -> R.color.select_icons to navController.navigate(R.id.action_bluetoothFragment_to_mapFragment)
                    R.id.bluetooth -> R.color.select_icons to navController.navigate(R.id.action_mapFragment_to_bluetoothFragment)
                    else -> R.color.white to ""
                }
            }
        })

    }

}