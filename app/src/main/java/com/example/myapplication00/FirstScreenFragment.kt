package com.example.myapplication00

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.example.myapplication00.databinding.FragmentFirstScreenBinding
import java.io.File

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

        val button = binding.button
        val addNewWord = binding.addNewWord
        val newWord = binding.newWord

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        folder = File(path,"/KoloFortuny")
        file = File(folder, "/words.txt")

        createFile()

        button.setOnClickListener{
            val action = R.id.action_firstScreenFragment_to_secondScreenFragment
            Navigation.findNavController(binding.root).navigate(action)
        }

        addNewWord.setOnClickListener {
            file.appendText("${binding.newWord.text.toString().uppercase()}\n${binding.newWordCategory.text.toString().uppercase()} \n")
            val toast = Toast.makeText(this.context, "Dodano hasło ${binding.newWord.text.toString()} w kategorii ${binding.newWordCategory.text.toString()}", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    private fun createFile(){
        if(!folder.exists()) {
            folder.mkdir()

            val basicWords: Array<Pair<String, String>> = arrayOf(Pair("MIESZKO I", "HISTORIA POLSKI"), Pair("HUSARIA", "HISTORIA POLSKI"),
                Pair("ROBERT LEWANDOWSKI", "PIŁKA NOŻNA"), Pair("SPALONY", "PIŁKA NOŻNA"), Pair("KARTA GRAFICZNA", "KOMPUTER"), Pair("PROCESOR", "KOMPUTER"),
                Pair("MINECRAFT", "GRY KOMPUTEROWE"), Pair("SONIC", "GRY KOMPUTEROWE"), Pair( "MONSUN", "POGODA"), Pair( "CYKLON", "POGODA"))

            var i: Int = 0

            while ( i < 10 ) {
                file.appendText("${basicWords[i].first}\n${basicWords[i].second} \n")
                i++
            }
        }
    }
}