package com.example.myapplication00

import android.Manifest
import android.annotation.SuppressLint
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
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import kotlin.system.exitProcess

class BluetoothPairingFragment: Fragment(), AdapterExample.OnItemClickListener{
    lateinit var binding: FragmentBluetoothPairingBinding
    val MESSAGE_READ: Int = 0
    val MESSAGE_WRITE: Int = 1
    val MESSAGE_TOAST: Int = 2

    private var mBluetoothAdapter: BluetoothAdapter? = null
    public var mBluetoothSocket: BluetoothSocket? = null

    var exampleList : MutableList<ItemExample> = mutableListOf()
    var adapter = AdapterExample(exampleList, this)

    var mUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    lateinit var receivedMessage: String
    lateinit var sendMessage: String
    private lateinit var connectedThread: ConnectedThread

    lateinit var mAddress: String

    private lateinit var mmRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBluetoothPairingBinding.inflate(layoutInflater)

        //getting bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        val discoverableIntent: Intent =
            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
        startActivityForResult(discoverableIntent, 1)

        //checking for bluetooth compatibility and activity
        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this.context, "Bluetooth is not supported" , Toast.LENGTH_SHORT).show()
            exitProcess(-1)
        }else if (mBluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1);
        }

        //registering the receiver
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        activity?.registerReceiver(mReceiver, filter)

        val activationThread = connectedActivationThread()
        activationThread.start()

        binding.searchButton.setOnClickListener {
            var tmp = mBluetoothAdapter?.startDiscovery()

            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
            binding.recyclerView.setHasFixedSize(true)
        }

        binding.startGameButton.setOnClickListener{
            if(mBluetoothSocket != null) {
                if (mBluetoothSocket!!.isConnected) {
                    connectedThread.write("StartGame".toByteArray())

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
        val bluetoothServer = AcceptThread()
        bluetoothServer.start()
    }

    override fun onDestroy() {
        activity?.unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    override fun onItemClick(position: Int) {
        mAddress = exampleList[position].secondLine
        val device = mBluetoothAdapter!!.getRemoteDevice(mAddress)
        val connection = ConnectThread(device)
        connection.run()
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
                    Toast.makeText(this@BluetoothPairingFragment.context, "${receivedMessage}", Toast.LENGTH_SHORT).show()
                    if (receivedMessage == "StartGame"){
                        val action = R.id.action_bluetoothPairingFragment_to_secondScreenFragment
                        Navigation.findNavController(binding.root).navigate(action)
                    }
                }
                MESSAGE_TOAST -> {
                }
            }
        }
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
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

    private inner class connectedActivationThread: Thread() {
        override fun run(){
            while(mBluetoothSocket == null){
            }

            while(mBluetoothSocket != null){
                if(mBluetoothSocket!!.isConnected){
                    connectedThread = ConnectedThread(mBluetoothSocket!!)
                    connectedThread.start()
                    break
                }
            }
        }
    }

    private inner class AcceptThread : Thread() {

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            mBluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord("KoloFortuny", mUUID)
        }

        val handler: Handler = Handler()

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Toast.makeText(this@BluetoothPairingFragment.context, "Socket's accept() method failed", Toast.LENGTH_SHORT).show()
                    Log.e("BTconnection", "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    mBluetoothSocket = it
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e("BTconnection", "Could not close the connect socket", e)
            }
        }
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(mUUID)
        }

        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter?.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                mBluetoothSocket = socket
                if(mBluetoothSocket!!.isConnected){
                    Toast.makeText(this@BluetoothPairingFragment.context, "Udało się sparować z ${mBluetoothSocket!!.remoteDevice.name}", Toast.LENGTH_SHORT).show()
                }
                //Toast.makeText(applicationContext, "${mBluetoothSocket!!.isConnected}", Toast.LENGTH_SHORT).show()
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e("BT Client Connection: ", "Could not close the client socket", e)
            }
        }
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

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