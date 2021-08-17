package com.example.myapplication00

import android.content.Intent
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
import java.lang.StringBuilder
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
    var moneyCache: Int = 0
    var phaseNumber = 1
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

        val intent = Intent(this.context, PopUpWindow::class.java)

        binding.goBackButton.setOnClickListener {
            Navigation.findNavController(view).popBackStack()
        }

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        folder = File(path,"/KoloFortuny")
        file = File(folder, "/words.txt")

        width = Resources.getSystem().displayMetrics.widthPixels

        readWord()

        intent.putExtra("popuptext", "Zakręć kołem fortuny!")
        intent.putExtra("darkstatusbar", false)
        startActivity(intent)

        binding.startWheelButton.setOnClickListener {
            if(phaseNumber == 1) {
                if (width == 1080)
                    pivot = 385
                else if (width == 1440)
                    pivot = 490
                if (!animationFlag) {
                    animationFlag = true
                    var rotationValue = Random.nextInt(1080, 1440)
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
                while (degrees > 360) {
                    degrees -= 360
                }

                wheelValue = wheelValues[rounding((degrees.toDouble() / 15))]

                Handler().postDelayed({
                    if (wheelValue != 0 && wheelValue != 1 && wheelValue != 3) {
                        moneyCache += wheelValue
                    } else if (wheelValue == 0) {
                        money = 0
                        moneyCache = 0
                        binding.moneyAccount.text = "Stan konta: ${money}$"
                    } else if (wheelValue == 1) {

                    }else if(wheelValue == 3){
                        moneyCache += 300
                    }
                }, 4000)
                phaseNumber = 2
            }else if(phaseNumber == 2){
                val toast = Toast.makeText(
                    this.context,
                    "Nie możesz teraz zakręcić kołem!",
                    Toast.LENGTH_SHORT
                )
                toast.show()
            }
        }

        binding.wordPushButton.setOnClickListener {
            if(phaseNumber == 2 || phaseNumber == 1) {
                if (binding.guessWord.text.toString().uppercase() == word) {
                    val toast =
                        Toast.makeText(this.context, "Brawo zgadłeś hasło!", Toast.LENGTH_SHORT)
                    toast.show()
                } else {
                    val toast = Toast.makeText(
                        this.context,
                        "Niestety nie zgadłeś hasła :(",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                }
                phaseNumber = 1
            }
        }

        letterButtons()
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

    fun checkLetter(letter: Char): Boolean{
        var temp = 1
        var j = 0
        for(i in 0..word.length - 1) {
            if(letter == word[i]){
                while(j < i){
                    temp += 3
                    j++
                }
                val text = StringBuilder(binding.word.text).also { it.setCharAt(temp,letter)}
                binding.word.text = text.toString()

                money += moneyCache
                binding.moneyAccount.text = "Stan konta: ${money}$"
                moneyCache = 0
                phaseNumber = 1
                return true
            }
        }
        moneyCache = 0
        phaseNumber = 1
        return false
    }

    fun letterButtons() {
        val toast = Toast.makeText(this.context, "Zakręć kołem!", Toast.LENGTH_SHORT)
        binding.LetterB.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('B')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterC.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('C')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterD.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('D')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterF.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('F')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterH.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('H')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterG.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('G')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterJ.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('J')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterK.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('K')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterL.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('L')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterM.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('M')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterN.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('N')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterP.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('P')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterR.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('R')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterS.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('S')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterT.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('T')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterV.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('V')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterW.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('W')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterX.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('X')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterZ.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('Z')
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
    }

}