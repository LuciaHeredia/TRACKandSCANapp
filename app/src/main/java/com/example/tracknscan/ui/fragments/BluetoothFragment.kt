package com.example.tracknscan.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tracknscan.databinding.FragmentBluetoothBinding
import com.example.tracknscan.helpers.throwToast
import com.example.tracknscan.adapters.DevicesAdapter
import com.example.tracknscan.data.bluetoothScan.BluetoothController
import com.example.tracknscan.helpers.Constants
import com.example.tracknscan.helpers.hasBluetoothPermission
import com.example.tracknscan.viewModel.bluetoothScan.BluetoothViewModel
import com.example.tracknscan.viewModel.bluetoothScan.BluetoothViewModelFactory

class BluetoothFragment : Fragment() {

    private var _binding: FragmentBluetoothBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BluetoothViewModel

    private lateinit var devicesAdapter: DevicesAdapter

    private var spinner: ProgressBar? = null

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        // ViewModelFactory to pass input data to the ViewModel
        viewModel = ViewModelProvider(this, BluetoothViewModelFactory(BluetoothController(requireContext())))[BluetoothViewModel::class.java]

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
        viewModel.devicesToShow.observe(viewLifecycleOwner
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

    @RequiresApi(Build.VERSION_CODES.S)
    private fun askBluetoothPermission() {

        // register for result - checking if user enabled bluetooth
        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(!requireContext().hasBluetoothPermission()) {
                throwToast(requireContext(), Constants.Bluetooth.THROW_BLUETOOTH_NOT_ENABLED)
            } else {
                spinner?.visibility = View.VISIBLE // show progress bar
                viewModel.startScanning()
            }
        }

        // register for result - Permissions(connect+scan)
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val bluetoothScanGranted = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions[Manifest.permission.BLUETOOTH_SCAN] == true
            } else true

            val bluetoothConnectGranted = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else true

            // permission to scan bluetooth devices -> granted
            if(bluetoothScanGranted && bluetoothConnectGranted) {
                if (!requireContext().hasBluetoothPermission()) {
                    // bluetooth disabled -> ask user to turn on
                    enableBluetoothLauncher.launch(
                        Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                    )
                    throwToast(requireContext(), Constants.Bluetooth.THROW_ENABLE_BLUETOOTH)

                } else {
                    spinner?.visibility = View.VISIBLE // show progress bar
                    viewModel.startScanning()
                }
            } else {
                throwToast(requireContext(), Constants.Bluetooth.THROW_BLUETOOTH_SCAN_PERMISSION_DENIED)
            }
        }

        // check whether app already has the permissions (enabled from SDK >= 31)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.stopScanning()
        viewModel.releaseDataReceiver()
    }

}