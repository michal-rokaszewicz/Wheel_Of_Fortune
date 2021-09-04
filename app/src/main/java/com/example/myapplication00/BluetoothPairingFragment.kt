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
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.system.exitProcess

class BluetoothPairingFragment: Fragment(), AdapterExample.OnItemClickListener{
    var STARTGAME: Int = 1

    val MESSAGE_READ: Int = 0
    val MESSAGE_WRITE: Int = 1
    val MESSAGE_TOAST: Int = 2

    lateinit var binding: FragmentBluetoothPairingBinding

    var exampleList : MutableList<ItemExample> = mutableListOf()
    var adapter = AdapterExample(exampleList, this)

    lateinit var connectedThread: ConnectedThread

    var receivedMessage: String = ""
    lateinit var sendMessage: String

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

        val activationThread = connectedActivationThread()
        activationThread.start()

        binding.searchButton.setOnClickListener {
            var tmp = (activity as MainActivity).mBluetoothAdapter?.startDiscovery()

            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
            binding.recyclerView.setHasFixedSize(true)
        }

        binding.startGameButton.setOnClickListener{
            if((activity as MainActivity).mBluetoothAdapter != null) {
                if (((activity as MainActivity).mBluetoothSocket)!!.isConnected) {

                        (activity as MainActivity).wordNumber = Random.nextInt(0, (activity as MainActivity).text.size - 1)
                    if ((activity as MainActivity).wordNumber != 0) {
                        if ((activity as MainActivity).wordNumber % 2 != 0)
                            (activity as MainActivity).wordNumber -= 1
                    }
                    connectedThread.write((activity as MainActivity).wordNumber.toString().toByteArray())
                   Handler().postDelayed({connectedThread.write("StartGame".toByteArray())
                       (activity as MainActivity).isHost = true
                       val action = R.id.action_bluetoothPairingFragment_to_secondScreenFragment
                       Navigation.findNavController(binding.root).navigate(action)}, 2000)
                }
            }
        }
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

    inner class connectedActivationThread: Thread() {
        override fun run(){
            while((activity as MainActivity).mBluetoothSocket == null){
            }

            while((activity as MainActivity).mBluetoothSocket != null){
                if((activity as MainActivity).mBluetoothSocket!!.isConnected){
                    connectedThread = ConnectedThread((activity as MainActivity).mBluetoothSocket!!)
                    connectedThread.start()
                    break
                }
            }
        }
    }

    val mHandler = @SuppressLint("HandlerLeak")
    object: Handler(){
        override fun handleMessage(msg: Message) {

            when(msg!!.what) {
                MESSAGE_WRITE -> {
                    val writeBuf = msg.obj as ByteArray
                    val writeMessage = String(writeBuf)
                    sendMessage = writeMessage
                }
                MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    val readMessage = String(readBuf, 0, msg.arg1)
                    receivedMessage = readMessage

                    Toast.makeText(this@BluetoothPairingFragment.context, receivedMessage, Toast.LENGTH_SHORT).show()

                    if(receivedMessage == "StartGame"){
                        val action = R.id.action_bluetoothPairingFragment_to_secondScreenFragment
                        Navigation.findNavController(binding.root).navigate(action)
                    }else if(receivedMessage == "YourTurn"){
                        (activity as MainActivity).phaseNumber = 1
                    }else if(receivedMessage >= "A" && receivedMessage <= "Z"){
                        (activity as MainActivity).opponentLetters += receivedMessage
                    }else{
                        (activity as MainActivity).wordNumber = receivedMessage.toInt()
                    }
                }
                MESSAGE_TOAST -> {
                }
            }
        }
    }

    inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d("BT Connection: ", "Input stream was disconnected", e)
                    break
                }
                // Send the obtained bytes to the UI activity.
                val readMsg = mHandler.obtainMessage(
                    MESSAGE_READ, numBytes, -1,
                    mmBuffer)
                readMsg.sendToTarget()
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e("BT Connection: ", "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = mHandler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                mHandler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = mHandler.obtainMessage(
                MESSAGE_WRITE, -1, -1, mmBuffer)
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e("BT Connection: ", "Could not close the connect socket", e)
            }
        }
    }

}