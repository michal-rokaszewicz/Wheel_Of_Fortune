package com.example.myapplication00

import android.content.res.Resources
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.myapplication00.databinding.FragmentSecondScreenBinding
import java.io.File
import java.io.InputStream
import kotlin.random.Random

class SecondScreenFragment : Fragment(){
    lateinit var binding: FragmentSecondScreenBinding
    var animationFlag: Boolean = false
    var degrees: Int = 0
    var width: Int = 0
    var pivot: Int = 0
    val wheelValues: Array<Int> = arrayOf(1, 300, 400, 600, 0, 900, 3, 500, 900, 300, 400, 550, 800, 500, 300, 500, 600, 2500, 600, 300, 700, 450, 350, 800)
    var wheelValue: Int = 1000
    var money: Int = 0
    var word: String = ""
    //file
    lateinit var path: File
    lateinit var folder: File
    lateinit var file: File

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

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        folder = File(path,"/KoloFortuny")
        file = File(folder, "/words.txt")

        val button = binding.goBackButton

        width = Resources.getSystem().displayMetrics.widthPixels

        readWord()

        button.setOnClickListener {
            Navigation.findNavController(view).popBackStack()
        }

        binding.startWheelButton.setOnClickListener{
            if(width == 1080)
                pivot = 385
            else if(width == 1440)
                pivot = 490
            if(!animationFlag) {
                animationFlag = true
                var rotationValue = Random.nextInt(1200, 1500)
                val rotation = RotateAnimation(
                    degrees.toFloat(),
                    (degrees + rotationValue).toFloat(), pivot.toFloat(), pivot.toFloat()
                )
                rotation.duration = 4000
                rotation.fillAfter = true
                rotation.interpolator = DecelerateInterpolator(0.8f)
                degrees += rotationValue
                binding.fortuneWheel.startAnimation(rotation)
                animationFlag = false
            }
            while(degrees > 360){
                degrees -= 360
            }

            wheelValue = wheelValues[rounding((degrees.toDouble()/15))]

            Handler().postDelayed({if(wheelValue != 0 && wheelValue != 1 && wheelValue != 3){
                money += wheelValue
                binding.moneyAccount.text = "Stan konta: ${money}$"
            }}, 4000)
        }
    }

    fun readWord(){
        val bufferedReader = file.bufferedReader()
        val text: List<String> = bufferedReader.readLines()

        var number: Int = Random.nextInt(0, text.size - 1)

        if(number != 0 ) {
            if(number % 2 != 0)
                number -= 1
        }

        var underlines: String = ""

        for(i in 0..text[number].length - 1) {
            underlines += " _ "
        }

        word = text[number]
        binding.category.text = text[number + 1]
        binding.word.text = underlines
    }


    fun rounding(number: Double): Int {
        var temp = number
        temp -= temp.toInt()
        if(temp > 0.49)
            if(number.toInt() + 1 == 24)
                return 0
            else
                return number.toInt() + 1
        else
            if(number.toInt() == 24)
                return 0
            else
                return number.toInt()
    }
}