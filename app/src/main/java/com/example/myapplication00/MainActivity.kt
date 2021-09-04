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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.bt.AdapterExample
import com.example.bt.ItemExample
import com.example.myapplication00.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val REQUEST_ENABLE_BT = 1

    //variable which indicates game phases like spinning wheel or guessing letters ect.
    var phaseNumber = 1
    var opponentLetters = ""

    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val folder = File(path, "/KoloFortuny")
    val file = File(folder, "/words.txt")

    var mBluetoothAdapter: BluetoothAdapter? = null
    var mBluetoothSocket: BluetoothSocket? = null
    var isHost: Boolean = false
    var wordNumber = 0
    val bufferedReader = file.bufferedReader()
    val text: List<String> = bufferedReader.readLines()
    var mUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    lateinit var mAddress: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //check permissions
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) && (ContextCompat.checkSelfPermission(    //checking for location access
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ){
            val permissions = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            // if is denied, ask for it
            ActivityCompat.requestPermissions(this, permissions, 0)
        }

        bluetoothInit()
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    fun bluetoothInit(){
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(application, "BT is not supported" , Toast.LENGTH_SHORT).show()
        }
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }



    inner class AcceptThread : Thread() {

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
                    Toast.makeText(this@MainActivity, "Socket's accept() method failed", Toast.LENGTH_SHORT).show()
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

    inner class ConnectThread(device: BluetoothDevice) : Thread() {

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
                    Toast.makeText(this@MainActivity, "Udało się sparować z ${mBluetoothSocket!!.remoteDevice.name}", Toast.LENGTH_SHORT).show()
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
}