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

    //flag that indicates that animation is going
    var animationFlag: Boolean = false

    //variables for wheel animation
    var degrees: Int = 0
    var width: Int = 0
    var pivot: Int = 0

    //variables for wheel functionality
    val wheelValues: Array<Int> = arrayOf(1, 300, 400, 600, 0, 900, 3, 500, 900, 300, 400, 550, 800, 500, 300, 500, 600, 2500, 600, 300, 700, 450, 350, 800)
    var wheelValue: Int = 1000
    var money: Int = 0
    var moneyCache: Int = 0

    //variable which indicates game phases like spinning wheel or guessing letters ect.
    var phaseNumber = 1

    //variables which contains the word that player is trying to guess and letters which he tried to guess but missed
    var word: String = ""
    var missedLetters = ""

    //variable which indicates rounds of game (there are 5 rounds total)
    var round = 1

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

        //go back action while clicking button
        binding.goBackButton.setOnClickListener {
            Navigation.findNavController(view).popBackStack()
        }

        //assigning file paths
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        folder = File(path,"/KoloFortuny")
        file = File(folder, "/words.txt")

        //getting screen resolution
        width = Resources.getSystem().displayMetrics.widthPixels

        //drawing word from file
        readWord()

        if(round != 1) {
            popUp("Zakręć kołem fortuny!")
        }

        //wheel animation and functionality on clicking button
        binding.startWheelButton.setOnClickListener {
            if(phaseNumber == 1 || phaseNumber == 3) {
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
                        phaseNumber = 2
                        moneyCache += wheelValue
                    } else if (wheelValue == 0) {
                        money = 0
                        moneyCache = 0
                        binding.moneyAccount.text = "Stan konta: ${money}$"
                        popUp("Niestety zbankrutowałeś! Tracisz wszystkie środki")
                    } else if (wheelValue == 1) {
                        moneyCache = 0
                        popUp("Tracisz turę!")
                    }else if(wheelValue == 3){
                        moneyCache += 300
                        popUp("Wylosowałeś możliwość dodatkowego zakręcenia kołem!")
                    }
                }, 4000)
                if(wheelValue != 0 && wheelValue != 1 && wheelValue != 3)
                Handler().postDelayed({popUp("Zgadnij spółgłoskę!")}, 4000)

            }else if(phaseNumber == 2){
                val toast = Toast.makeText(
                    this.context,
                    "Nie możesz teraz zakręcić kołem!",
                    Toast.LENGTH_SHORT
                )
                toast.show()
            }
        }

        //guessing word functionality on clicking button
        binding.wordPushButton.setOnClickListener {
            if(phaseNumber == 3) {
                if (binding.guessWord.text.toString().uppercase() == word) {
                    if(round != 5) {
                        popUp("Brawo zgadłeś! Hasło to ${word} wygrywasz: ${money}$")
                        round++
                        missedLetters = ""
                        phaseNumber = 1
                        readWord()
                        moneyCache = 0
                        Handler().postDelayed({popUp("RUNDA ${round}")}, 2500)
                        binding.roundNumberText.text = "Runda ${round}"
                    }else if(round == 5){
                        popUp("Brawo odgadłeś wszystkie hasła! wygrywasz ${money}$")
                        Navigation.findNavController(view).popBackStack()
                    }
                } else {
                    popUp("Niestety nie udało ci się zgadnąć hasła!")
                }
                binding.guessWord.text.clear()
                phaseNumber = 1
            }else{
                val toast = Toast.makeText(
                    this.context,
                    "Nie możesz teraz zgadywać hasłą!",
                    Toast.LENGTH_SHORT
                )
                toast.show()
            }
        }

        letterButtons()
    }

    //function that draws word from file
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

    //function that changes wheel degree divided by 15 to table index
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

    //function which checks guessing letters
    fun checkLetter(letter: Char): Boolean{
        var letterFlag = 0
        var totalText = binding.word.text
        for(i in 0..missedLetters.length - 1){
            if(letter == missedLetters[i]){
                popUp("Już próbowałeś zgadnąć tą literę! spróbuj inną!")
                return false
            }
        }

        for(i in 0..binding.word.text.length - 1){
            if(letter == binding.word.text[i]){
                popUp("Już zgadłeś tą literę! Spróbuj innej!")
                return false
            }
        }

        var temp = 1
        var j = 0
        for(i in 0..word.length - 1) {
            if(letter == word[i]){
                letterFlag = 1
                while(j < i){
                    temp += 3
                    j++
                }
                val text = StringBuilder(totalText).also { it.setCharAt(temp,letter)}
                totalText = text.toString()
            }
        }
        if(letterFlag == 1){
            binding.word.text = totalText.toString()
            popUp("Brawo! Zgadłeś spółgłoskę, kwota: ${moneyCache}$ ląduje na twoim koncie!")
            money += moneyCache
            binding.moneyAccount.text = "Stan konta: ${money}$"
            moneyCache = 0
            phaseNumber = 3
            Handler().postDelayed({popUp("Zakręć kołem ponownie lub spróbuj zgadnąć hasło!")}, 2500)
            return true
        }
        missedLetters += letter
        popUp("Niestety nie udało się zgadnąć spółgłoski. Kwota: ${moneyCache}$ przepada!")
        moneyCache = 0
        phaseNumber = 3
        Handler().postDelayed({popUp("Zakręć kołem ponownie lub spróbuj zgadnąć hasło!")}, 2500)
        return false
    }

    //function which implements letter buttons actions
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

    //function that makes popup screens
    fun popUp(text: String){
        val intent = Intent(this.context, PopUpWindow::class.java)
        intent.putExtra("popuptext", text)
        intent.putExtra("darkstatusbar", false)
        startActivity(intent)
    }
}