package com.example.myapplication00

import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.myapplication00.databinding.FragmentFirstScreenBinding
import com.example.myapplication00.databinding.FragmentSecondScreenBinding
import java.io.File

class SecondScreenFragment : Fragment() {
    lateinit var binding: FragmentSecondScreenBinding

    //file
    lateinit var path: File
    lateinit var folder: File
    lateinit var file: File

    //word
    var wWord: String = ""
    var wCat: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSecondScreenBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        folder = File(path,"/KoloFortuny")
        file = File(folder, "/words.txt")

        val button = binding.goBackButton
        val category = binding.category
        val word = binding.word

        readWord()
        category.text = wCat
        word.text = wWord

        button.setOnClickListener {
            Navigation.findNavController(view).popBackStack()
        }
    }

    fun readWord(){
        var number: Int = (0..9).random()

        var text: List<String> = file.readLines()
        var line: String
        var n: Int = 0
        var w: String = ""
        var ww: String

        for( line in text){
            if(n == number){
                w = line
            }
        }

        var i: Int = 0
        var tmp: String = "w"

        while(i < w.length){
            if(w[i].toInt() != 9)
                tmp += w[i]
            else {
                wWord = tmp
                tmp = "s"
            }
        }
        wCat = tmp
    }

}