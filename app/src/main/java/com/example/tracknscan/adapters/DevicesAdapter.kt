package com.example.tracknscan.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tracknscan.R
import com.example.tracknscan.model.bluetoothScan.BluetoothDeviceDomain

class DevicesAdapter: RecyclerView.Adapter<DevicesAdapter.MyViewHolder>() {

    private var devicesList: List<BluetoothDeviceDomain> = emptyList()

    fun setDevicesList(data: List<BluetoothDeviceDomain>) {
        this.devicesList = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater =
            LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_device_row, parent, false)

        return MyViewHolder(inflater)
    }

    override fun getItemCount(): Int {
        return devicesList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(devicesList[position])
    }

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val deviceData: TextView = view.findViewById(R.id.device_data_text)

        fun bind(device: BluetoothDeviceDomain) {
            var dName = "null"
            val dAddress: String = device.address

            if(device.name != null)
                dName = device.name

            val tData: String = "Name: $dName , Address: $dAddress"
            deviceData.text = tData
        }

    }

}