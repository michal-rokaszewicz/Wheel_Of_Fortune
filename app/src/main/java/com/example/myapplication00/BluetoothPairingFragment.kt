package com.example.myapplication00

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent.getActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bt.AdapterExample
import com.example.bt.ItemExample
import com.example.myapplication00.databinding.FragmentBluetoothPairingBinding
import com.google.android.material.internal.ContextUtils.getActivity
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.system.exitProcess

class BluetoothPairingFragment: Fragment(), AdapterExample.OnItemClickListener{
    lateinit var binding: FragmentBluetoothPairingBinding

    var exampleList : MutableList<ItemExample> = mutableListOf()
    var adapter = AdapterExample(exampleList, this)

    private lateinit var mmRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBluetoothPairingBinding.inflate(layoutInflater)

        //getting bluetooth adapter

        val discoverableIntent: Intent =
            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
        startActivityForResult(discoverableIntent, 1)

        //registering the receiver
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        activity?.registerReceiver(mReceiver, filter)

        binding.searchButton.setOnClickListener {
            var tmp = (activity as MainActivity).mBluetoothAdapter?.startDiscovery()

            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
            binding.recyclerView.setHasFixedSize(true)
        }

        binding.startGameButton.setOnClickListener{
            if((activity as MainActivity).mBluetoothAdapter != null) {
                if (((activity as MainActivity).mBluetoothSocket)!!.isConnected) {
                    (activity as MainActivity).connectedThread.write("StartGame".toByteArray())
                    (activity as MainActivity).isHost = true
                    val action = R.id.action_bluetoothPairingFragment_to_secondScreenFragment
                    Navigation.findNavController(binding.root).navigate(action)
                }
            }
        }

/*
        binding.messageButton.setOnClickListener{
            if(mBluetoothSocket != null){
                if(mBluetoothSocket!!.isConnected){
                    sendMessage = binding.messageEditText.text.toString()
                    connectedThread.write(sendMessage.toByteArray())
                }
            }
        }

 */
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        //Starting to listen for client connection
        val bluetoothServer = (activity as MainActivity).AcceptThread()
        bluetoothServer.start()
    }

    override fun onDestroy() {
        activity?.unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    override fun onItemClick(position: Int) {
        (activity as MainActivity).mAddress = exampleList[position].secondLine
        val device = (activity as MainActivity).mBluetoothAdapter!!.getRemoteDevice((activity as MainActivity).mAddress)
        val connection = (activity as MainActivity).ConnectThread(device)
        connection.run()
    }

    val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                var temp = ItemExample("${device!!.name}", "${device.address}")
                if (!exampleList.contains(temp) && device.name != null) {
                    exampleList.add(temp)
                    binding.recyclerView.adapter?.notifyItemInserted(exampleList.indexOf(temp))
                    binding.recyclerView.adapter = adapter
                }

                Log.i(
                    "BT", """
                    ${device!!.name}
                    ${device.address}
                    """.trimIndent()
                )
            }
        }
    }



}