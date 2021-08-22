package com.example.myapplication00

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter.STATE_CONNECTED
import android.bluetooth.BluetoothProfile.STATE_CONNECTED
import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.provider.SyncStateContract
import android.util.Log
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
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.StringBuilder
import java.util.*
import kotlin.random.Random
import com.example.myapplication00.BluetoothActivity

class SecondScreenFragment : Fragment(){
    lateinit var binding: FragmentSecondScreenBinding

    //flag that indicates that animation is going
    var animationFlag: Boolean = false

    //variables for wheel animation
    var degrees: Int = 0

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

    var chosenWords: List<String> = emptyList()

    public var btMessage: String = ""

    //file
    lateinit var path: File
    lateinit var folder: File
    lateinit var file: File

    //handler
    private lateinit var mRunnable: Runnable
    private lateinit var mHandler: Handler

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

        //
        mHandler = Handler()

        mRunnable = Runnable {

            val bluetoothThing = BluetoothActivity()
            bluetoothThing.receiveCommand()

            if(bluetoothThing.output != null){
                Toast.makeText(this.context, "${bluetoothThing.output}", Toast.LENGTH_SHORT).show()
            }

            mHandler.postDelayed(
                mRunnable,
                100
            )
        }
        mHandler.postDelayed(
            mRunnable,
            100
        )
        //

        //go back action while clicking button
        binding.goBackButton.setOnClickListener {
            Navigation.findNavController(view).popBackStack()
        }

        //assigning file paths
        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        folder = File(path,"/KoloFortuny")
        file = File(folder, "/words.txt")

        //drawing word from file
        readWord()

        if(round != 1) {
            popUp("Zakręć kołem fortuny!")
        }

        //wheel animation and functionality on clicking button
        binding.startWheelButton.setOnClickListener {
            if(phaseNumber == 1 || phaseNumber == 3) {
                if (!animationFlag) {
                    animationFlag = true
                    var rotationValue = Random.nextInt(1080, 1440)
                    val rotation = RotateAnimation(
                        degrees.toFloat(),
                        (degrees + rotationValue).toFloat(), binding.fortuneWheel.pivotX, binding.fortuneWheel.pivotY
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
                        resetLetterButtonsColor()
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
                    "Nie możesz teraz zgadywać hasła!",
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

        while(chosenWords.contains(text[number])){
            number = Random.nextInt(0, text.size - 1)
        }


        if(number != 0 ) {
            if(number % 2 != 0)
                number -= 1
        }

        var underlines: String = ""

        for(i in 0..text[number].length - 1) {
            if(text[number][i] == ' ')
                underlines += "   "
            else
                underlines += " _ "
        }

        word = text[number]
        chosenWords.plus(word)
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
                binding.LetterB.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterC.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('C')
                binding.LetterC.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterD.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('D')
                binding.LetterD.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterF.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('F')
                binding.LetterF.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterH.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('H')
                binding.LetterH.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterG.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('G')
                binding.LetterG.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterJ.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('J')
                binding.LetterJ.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterK.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('K')
                binding.LetterK.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterL.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('L')
                binding.LetterL.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterM.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('M')
                binding.LetterM.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterN.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('N')
                binding.LetterN.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterP.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('P')
                binding.LetterP.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterR.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('R')
                binding.LetterR.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterS.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('S')
                binding.LetterS.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterT.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('T')
                binding.LetterT.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterV.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('V')
                binding.LetterV.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterW.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('W')
                //binding.LetterW.setBackgroundColor(Color.rgb(70,70,70))
                binding.LetterW.isEnabled = false
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterX.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('X')
                binding.LetterX.setBackgroundColor(Color.rgb(70,70,70))
            }else if(phaseNumber == 1){
                toast.show()
            }
        }
        binding.LetterZ.setOnClickListener {
            if(phaseNumber == 2) {
                checkLetter('Z')
                binding.LetterZ.setBackgroundColor(Color.rgb(70,70,70))
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

    fun resetLetterButtonsColor(){
        binding.LetterB.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterC.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterD.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterF.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterG.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterH.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterJ.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterK.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterL.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterM.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterN.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterP.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterR.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterS.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterT.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterX.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterZ.setBackgroundColor(Color.rgb(12, 82, 168))
        //binding.LetterW.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterV.setBackgroundColor(Color.rgb(12, 82, 168))
        binding.LetterW.isEnabled = true
    }
/*
    val handler: Handler = object : Handler() {
        public override fun handleMessage(msg: Message){
            when(msg.what){
                STATE_LISTENING -> {

                }
                STATE_MESSAGE_RECEIVED -> {
                    var readBuff: Byte = msg.obj as Byte
                    var tempMsg: String = readBuff.toString() + '0'+ msg.arg1.toString()
                    btMessage = tempMsg
                }
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
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }

                // Send the obtained bytes to the UI activity.
                val readMsg = handler.obtainMessage(
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
                Log.e(TAG, "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = handler.obtainMessage(
                MESSAGE_WRITE, -1, -1, mmBuffer)
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

 */
}