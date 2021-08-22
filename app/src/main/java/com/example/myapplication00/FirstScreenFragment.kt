package com.example.myapplication00

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.myapplication00.databinding.FragmentFirstScreenBinding
import java.io.File

class FirstScreenFragment : Fragment() {
    lateinit var binding: FragmentFirstScreenBinding
    lateinit var bluetoothAdapter: BluetoothAdapter

    //file
    lateinit var path: File
    lateinit var folder: File
    lateinit var file: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //checking for bluetooth compatibility
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bluetoothAdapter == null) {
            var toast = Toast.makeText(this.context, "Twoje urządzenie nie wspiera bluetooth!", Toast.LENGTH_LONG)
            toast.show()
            Handler().postDelayed({System.exit(-1)}, 2500)
        }

        binding = FragmentFirstScreenBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //assigning some buttons
        val button = binding.button
        val addNewWord = binding.addNewWord
        val newWord = binding.newWord

        //assigning file paths
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        folder = File(path,"/KoloFortuny")
        file = File(folder, "/words.txt")

        createFile()

        //action when clicking main button on first screen
        button.setOnClickListener{
            //checking for bluetooth connection
            if(bluetoothAdapter.isEnabled){
                val devices = bluetoothAdapter.bondedDevices
                if(devices.isEmpty() ){
                    var toast = Toast.makeText(this.context, "Nie jesteś połączony z przeciwnikiem! Połącz się z przeciwnikiem po bluetooth!", Toast.LENGTH_LONG)
                    toast.show()
                }else{
                    //giving popup screens for user
                    val intent = Intent(this.context, PopUpWindow::class.java)
                    intent.putExtra("popuptext", "Runda 1")
                    intent.putExtra("darkstatusbar", false)
                    startActivity(intent)

                    Handler().postDelayed({intent.putExtra("popuptext", "Zakręć kołem!")
                        intent.putExtra("darkstatusbar", false)
                        startActivity(intent)}, 2500)

                    //going to second screen and starting the game
                    val action = R.id.action_firstScreenFragment_to_secondScreenFragment
            Navigation.findNavController(binding.root).navigate(action)
                }
            }
        }

        //action on clicking button which adds new word and category
        addNewWord.setOnClickListener {
            file.appendText("${binding.newWord.text.toString().uppercase()}\n${binding.newWordCategory.text.toString().uppercase()} \n")
            val toast = Toast.makeText(this.context, "Dodano hasło ${binding.newWord.text.toString()} w kategorii ${binding.newWordCategory.text.toString()}", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    //function which creates file with basic words
    private fun createFile() {
            if (!folder.exists()) {
                folder.mkdir()

                val basicWords: Array<Pair<String, String>> = arrayOf(
                    Pair("MIESZKO I", "HISTORIA POLSKI"),
                    Pair("HUSARIA", "HISTORIA POLSKI"),
                    Pair("ROBERT LEWANDOWSKI", "PIŁKA NOŻNA"),
                    Pair("SPALONY", "PIŁKA NOŻNA"),
                    Pair("KARTA GRAFICZNA", "KOMPUTER"),
                    Pair("PROCESOR", "KOMPUTER"),
                    Pair("MINECRAFT", "GRY KOMPUTEROWE"),
                    Pair("SONIC", "GRY KOMPUTEROWE"),
                    Pair("MONSUN", "POGODA"),
                    Pair("CYKLON", "POGODA")
                )

                var i: Int = 0

                while (i < 10) {
                    file.appendText("${basicWords[i].first}\n${basicWords[i].second} \n")
                    i++
                }
            }
    }
}