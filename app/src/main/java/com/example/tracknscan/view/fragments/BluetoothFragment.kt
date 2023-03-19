package com.example.tracknscan.view.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.tracknscan.databinding.FragmentBluetoothBinding
import com.example.tracknscan.helpers.Constants
import com.example.tracknscan.view.activities.MainActivity
import com.example.tracknscan.viewModel.BluetoothViewModel


class BluetoothFragment : Fragment() {

    private var _binding: FragmentBluetoothBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<BluetoothViewModel>()

    private val bluetoothManager by lazy {
        requireContext().getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBluetoothBinding
            .inflate(inflater, container, false)
            .apply {
                this.vm = viewModel
            }

        if(!isBluetoothEnabled) // bluetooth disabled -> ask user to turn on
            askBluetoothPermission()

        return binding.root
    }

    private fun askBluetoothPermission() {

        // register for result - checking user's choice
        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    viewModel.startScanning()
                }
                else -> {
                    bluetoothResultCanceled()
                }
            }
        }

        // register for result - Scan Permission
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            enableBluetoothLauncher.launch(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            )
        }

        // bluetooth scan capability ON (enabled from SDK >= 31)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                Manifest.permission.BLUETOOTH_SCAN
            )
        }

    }

    private fun bluetoothResultCanceled() {
        // User did not enable Bluetooth or an error occurred
        Log.d("Bluetooth", "BT not enabled")

        // go to Map Fragment
        (activity as MainActivity?)!!.backToFrag(Constants.mapIdFragment)

        Toast.makeText(
            activity, "Bluetooth not enabled, try again later.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}