package com.example.tracknscan.view.fragments

import android.Manifest
import android.annotation.SuppressLint
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
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tracknscan.databinding.FragmentBluetoothBinding
import com.example.tracknscan.helpers.Constants
import com.example.tracknscan.model.bluetoothScan.DevicesAdapter
import com.example.tracknscan.model.bluetoothScan.data.AndroidBluetoothController
import com.example.tracknscan.view.activities.MainActivity
import com.example.tracknscan.viewModel.BluetoothViewModel
import com.example.tracknscan.viewModel.BluetoothViewModelFactory


class BluetoothFragment : Fragment() {

    private var _binding: FragmentBluetoothBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BluetoothViewModel

    private val bluetoothManager by lazy {
        requireContext().getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    private lateinit var devicesAdapter: DevicesAdapter

    var spinner: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        // ViewModelFactory to pass input data to the ViewModel
        viewModel = ViewModelProvider(this, BluetoothViewModelFactory(AndroidBluetoothController(requireContext())))[BluetoothViewModel::class.java]

        _binding = FragmentBluetoothBinding
            .inflate(inflater, container, false)
            .apply {
                this.vm = viewModel
            }

        initRecyclerView()
        askBluetoothPermission()
        observeNewDevicesScanned()
        filterButtonListener()

        return binding.root
    }

    private fun initRecyclerView(){
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            devicesAdapter = DevicesAdapter()
            adapter = devicesAdapter
        }

        // init progress bar
        spinner = binding.bProgressBar
        spinner!!.visibility = View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeNewDevicesScanned() {
        viewModel.state.observe(viewLifecycleOwner
        ) { devices ->
            devicesAdapter.setDevicesList(devices)
            devicesAdapter.notifyDataSetChanged()
        }
    }

    private fun filterButtonListener() {
        binding.bFilterButton.setOnClickListener {
            val addressToFilter = binding.bFilterText.text.toString()

            if(addressToFilter.isEmpty()) {
                spinner?.visibility = View.VISIBLE // show progress bar
            } else {
                spinner!!.visibility = View.GONE // hide progress bar
            }

            viewModel.filterList(addressToFilter)
        }
    }

    private fun askBluetoothPermission() {

        // register for result - checking user's choice
        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    spinner?.visibility = View.VISIBLE // show progress bar
                    viewModel.startScanning()
                }
                else -> {
                    bluetoothResultCanceled()
                }
            }
        }

        // register for result - Permissions(connect+scan)
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if( !isBluetoothEnabled) {
                // bluetooth disabled -> ask user to turn on
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            } else {
                spinner?.visibility = View.VISIBLE // show progress bar
                viewModel.startScanning()
            }
        }

        // bluetooth connect+scan capability ON (enabled from SDK >= 31)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
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
        viewModel.stopScanning()
        viewModel.releaseDataReceiver()
    }


}