package com.example.myapplication00

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.myapplication00.databinding.FragmentFirstScreenBinding
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FirstScreenFragment : Fragment() {
    lateinit var binding: FragmentFirstScreenBinding

    //file
    lateinit var path: File
    lateinit var folder: File
    lateinit var file: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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
                    //going to second screen and starting the game
                    val action = R.id.action_firstScreenFragment_to_bluetoothPairingFragment
            Navigation.findNavController(binding.root).navigate(action)
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