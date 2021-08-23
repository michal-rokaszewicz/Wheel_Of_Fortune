package com.example.myapplication00

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.content.AsyncTaskLoader
import kotlinx.coroutines.selects.select
import java.io.IOException
import java.util.*

class BluetoothActivity: AppCompatActivity() {

    public lateinit var output: ByteArray

    companion object{
        var myUUID: UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66")
        var bluetoothSocket: BluetoothSocket? = null
        lateinit var progress: ProgressDialog
        lateinit var bluetoothAdapter: BluetoothAdapter
        var isConnected: Boolean = false
        lateinit var address: String
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        address = bluetoothAdapter.address
        //Toast.makeText(this, "bluetooth is running", Toast.LENGTH_SHORT).show()
    }

    public fun sendCommand(input: String){
        if(bluetoothSocket != null){
            try{
                bluetoothSocket!!.outputStream.write(input.toByteArray())
            }catch(e: IOException){
                e.printStackTrace()
            }
        }
    }

    public fun receiveCommand(){
        if(bluetoothSocket != null){
            try{
                Toast.makeText(this, "dupa", Toast.LENGTH_SHORT).show()
                output = bluetoothSocket!!.inputStream.readBytes()
            }catch(e: IOException){
                e.printStackTrace()
            }
        }
    }

    public fun disconnect(){
        if(bluetoothSocket != null){
            try{
                bluetoothSocket!!.close()
                bluetoothSocket = null
                isConnected = false
            }catch (e: IOException){
             e.printStackTrace()
            }
        }
        finish()
    }

    private class ConnectToDevice(c: Context): AsyncTask<Void, Void, String>(){
        private var connectSuccess: Boolean = true
        private val context: Context

        init{
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progress = ProgressDialog.show(context, "Connecting...", "Please wait")
        }

        override fun doInBackground(vararg params: Void?): String? {
            try{
                if(bluetoothSocket == null || !isConnected){
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    bluetoothSocket!!.connect()
                }
            }catch (e: IOException){
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(!connectSuccess){
                Log.i("data", "couldn't connect")
            }else{
                isConnected = true
            }
            progress.dismiss()
        }
    }
}