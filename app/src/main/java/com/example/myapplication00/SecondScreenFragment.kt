package com.example.myapplication00

import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
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

const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2

class SecondScreenFragment : Fragment() {
    lateinit var binding: FragmentSecondScreenBinding

    //flag that indicates that animation is going
    var animationFlag: Boolean = false

    //variables for wheel animation
    var degrees: Int = 0

    //variables for wheel functionality
    val wheelValues: Array<Int> = arrayOf(
        1,
        300,
        400,
        600,
        0,
        900,
        3,
        500,
        900,
        300,
        400,
        550,
        800,
        500,
        300,
        500,
        600,
        2500,
        600,
        300,
        700,
        450,
        350,
        800
    )
    var wheelValue: Int = 1000
    var money: Int = 0
    var moneyCache: Int = 0

    var receivedMessage: String = ""
    lateinit var sendMessage: String
    lateinit var connectedThread: SecondConnectedThread
    lateinit var nextTurnThread: NextTurnThread
    lateinit var handler: Handler
    lateinit var runnable: Runnable
    var isRunning = false

    //variables which contains the word that player is trying to guess and letters which he tried to guess but missed
    var word: String = ""
    var missedLetters = ""

    var chosenWords: List<String> = emptyList()

    var round = 1

    //assigning file paths
    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val folder = File(path, "/KoloFortuny")
    val file = File(folder, "/words.txt")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSecondScreenBinding.inflate(layoutInflater)
        val view = binding.root
        connectedThread = SecondConnectedThread((activity as MainActivity).mBluetoothSocket!!)
        connectedThread.start()
        nextTurnThread = NextTurnThread()
        nextTurnThread.start()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler()

        runnable = Runnable {
            if(isRunning) {
                if(round + 1 == (activity as MainActivity).round){

                    binding.roundNumberText.text = ("Runda ${(activity as MainActivity).round}")
                    Handler().postDelayed({
                        if((activity as MainActivity).roundPop) {
                            popUp("Runda ${(activity as MainActivity).round}!")
                            (activity as MainActivity).roundPop = false
                        }
                        readWord((activity as MainActivity).wordNumber)
                        round++
                        resetLetterButtonsColor()}, 1000)
                }
                if ((activity as MainActivity).phaseNumber == 1) {
                    popUp("Twoja tura, zakręć kołem fortuny!")
                    for (i in (activity as MainActivity).opponentLetters) {
                        checkLetter(i, false)
                        when(i){
                         'B' -> binding.LetterB.isEnabled = false
                         'C' -> binding.LetterC.isEnabled = false
                         'D' -> binding.LetterD.isEnabled = false
                         'F' -> binding.LetterF.isEnabled = false
                         'G' -> binding.LetterG.isEnabled = false
                         'H' -> binding.LetterH.isEnabled = false
                         'J' -> binding.LetterJ.isEnabled = false
                         'K' -> binding.LetterK.isEnabled = false
                         'L' -> binding.LetterL.isEnabled = false
                         'M' -> binding.LetterM.isEnabled = false
                         'N' -> binding.LetterN.isEnabled = false
                         'P' -> binding.LetterP.isEnabled = false
                         'R' -> binding.LetterR.isEnabled = false
                         'S' -> binding.LetterS.isEnabled = false
                         'T' -> binding.LetterT.isEnabled = false
                         'X' -> binding.LetterX.isEnabled = false
                         'Z' -> binding.LetterZ.isEnabled = false
                         'W' -> binding.LetterW.isEnabled = false
                         'V' -> binding.LetterV.isEnabled = false
                        }
                    }
                    isRunning = false
                }
            }
            handler.postDelayed(runnable, 100)
        }
        handler.postDelayed(runnable, 100)

        if ((activity as MainActivity).round == 1) {
            readWord((activity as MainActivity).wordNumber)
            if (!(activity as MainActivity).isHost) {
                (activity as MainActivity).phaseNumber = 4
            }
        } else {
            if ((activity as MainActivity).isHost) {
                (activity as MainActivity).wordNumber = readWord()
                connectedThread.write("${(activity as MainActivity).wordNumber}".toByteArray())

            } else {
                Handler().postDelayed({
                    (activity as MainActivity).wordNumber = receivedMessage.toInt()
                    readWord((activity as MainActivity).wordNumber)
                }, 1000)
                (activity as MainActivity).phaseNumber = 4
            }
        }

        //giving popup screens for user
        val intent = Intent(this.context, PopUpWindow::class.java)
        intent.putExtra("popuptext", "Runda 1")
        intent.putExtra("darkstatusbar", false)
        startActivity(intent)

        if((activity as MainActivity).phaseNumber != 4) {
            Handler().postDelayed({
                intent.putExtra("popuptext", "Zakręć kołem!")
                intent.putExtra("darkstatusbar", false)
                startActivity(intent)
            }, 2500)
        }

        //go back action while clicking button
        binding.goBackButton.setOnClickListener {
            val action = R.id.action_secondScreenFragment_to_firstScreenFragment
            Navigation.findNavController(binding.root).navigate(action)
        }

        if ((activity as MainActivity).round != 1 && (activity as MainActivity).phaseNumber != 4) {
            popUp("Zakręć kołem fortuny!")
        }

        //wheel animation and functionality on clicking button
        binding.startWheelButton.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 1) {
                    if (!animationFlag) {
                        animationFlag = true
                        var rotationValue = Random.nextInt(1080, 1440)
                        val rotation = RotateAnimation(
                            degrees.toFloat(),
                            (degrees + rotationValue).toFloat(),
                            binding.fortuneWheel.pivotX,
                            binding.fortuneWheel.pivotY
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
                            (activity as MainActivity).phaseNumber = 2
                            moneyCache += wheelValue
                        } else if (wheelValue == 0) {
                            money = 0
                            moneyCache = 0
                            binding.moneyAccount.text = "Stan konta: ${money}$"
                            popUp("Niestety zbankrutowałeś! Tracisz wszystkie środki")
                        } else if (wheelValue == 1) {
                            moneyCache = 0
                            popUp("Tracisz turę!")
                            (activity as MainActivity).phaseNumber = 4
                            connectedThread.write("YourTurn".toByteArray())
                        } else if (wheelValue == 3) {
                            moneyCache += 300
                            popUp("Wylosowałeś możliwość dodatkowego zakręcenia kołem!")
                        }
                    }, 4000)
                    if (wheelValue != 0 && wheelValue != 1 && wheelValue != 3)
                        Handler().postDelayed({ popUp("Zgadnij spółgłoskę!") }, 4000)

                } else if ((activity as MainActivity).phaseNumber == 2 || (activity as MainActivity).phaseNumber == 3) {
                    val toast = Toast.makeText(
                        this.context,
                        "Nie możesz teraz zakręcić kołem!",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                }
            }else {
                Toast.makeText(this.context, "Teraz jest tura przeciwnika!", Toast.LENGTH_SHORT).show()
            }
        }

        //guessing word functionality on clicking button
        binding.wordPushButton.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 3) {
                    if (binding.guessWord.text.toString().uppercase() == word) {
                        if ((activity as MainActivity).round != 5) {
                            popUp("Brawo zgadłeś! Hasło to ${word} wygrywasz: ${money}$")
                            (activity as MainActivity).round++
                            round++
                            missedLetters = ""
                            (activity as MainActivity).opponentLetters = ""
                            connectedThread.write("NextRound".toByteArray())
                            (activity as MainActivity).phaseNumber = 1
                            var wordNumber = readWord()
                            Handler().postDelayed({connectedThread.write(wordNumber.toString().toByteArray())}, 500)
                            moneyCache = 0
                            resetLetterButtonsColor()
                            Handler().postDelayed({ popUp("RUNDA ${(activity as MainActivity).round}") }, 2500)
                            binding.roundNumberText.text = "Runda ${(activity as MainActivity).round}"
                        } else if ((activity as MainActivity).round == 5) {
                            popUp("Brawo odgadłeś wszystkie hasła! wygrywasz ${money}$")
                            val action = R.id.action_secondScreenFragment_to_firstScreenFragment
                            Navigation.findNavController(binding.root).navigate(action)
                        }
                    } else {
                        popUp("Niestety nie udało ci się zgadnąć hasła!")
                        connectedThread.write("YourTurn".toByteArray())
                        (activity as MainActivity).phaseNumber = 4
                    }
                    binding.guessWord.text.clear()
                    //(activity as MainActivity).phaseNumber = 1
                } else {
                    val toast = Toast.makeText(
                        this.context,
                        "Nie możesz teraz zgadywać hasła!",
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                }
            }else{
                Toast.makeText(this.context, "Teraz jest tura przeciwnika!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.nextTurnButton.setOnClickListener{
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 3) {
                    connectedThread.write("YourTurn".toByteArray())
                    (activity as MainActivity).phaseNumber = 4
                } else {
                    Toast.makeText(
                        this.context,
                        "Nie możesz teraz tego zrobić!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else{
                Toast.makeText(this.context, "Teraz jest tura przeciwnika!", Toast.LENGTH_SHORT).show()
            }
        }

        letterButtons()
    }

    //function that draws word from file
    fun readWord(mNumber: Int = -1):Int {
        val bufferedReader = file.bufferedReader()
        val text: List<String> = bufferedReader.readLines()

        var number: Int = Random.nextInt(0, text.size - 1)
        while (chosenWords.contains(text[number])) {
            number = Random.nextInt(0, text.size - 1)
        }
        if (number != 0) {
            if (number % 2 != 0)
                number -= 1
        }

        if (mNumber != -1){
            number = mNumber
        }
        word = text[number]

        var underlines: String = ""

        for (i in 0..word.length - 1) {
            if (word[i] == ' ')
                underlines += "   "
            else
                underlines += " _ "
        }

        chosenWords.plus(word)
        binding.category.text = text[number + 1]
        binding.word.text = underlines
        return number
    }

    //function that changes wheel degree divided by 15 to table index
    fun rounding(number: Double): Int {
        var temp = number
        temp -= temp.toInt()
        if (temp > 0.49)
            if (number.toInt() + 1 == 24)
                return 0
            else
                return number.toInt() + 1
        else
            if (number.toInt() == 24)
                return 0
            else
                return number.toInt()
    }

    //function which checks guessing letters
    fun checkLetter(letter: Char, pop: Boolean): Boolean {
        var letterFlag = 0
        var totalText = binding.word.text
        for (i in 0..missedLetters.length - 1) {
            if (letter == missedLetters[i]) {
                if (pop)
                    popUp("Już próbowałeś zgadnąć tą literę! spróbuj inną!")
                return false
            }
        }

        for (i in 0..binding.word.text.length - 1) {
            if (letter == binding.word.text[i]) {
                if (pop)
                    popUp("Już zgadłeś tą literę! Spróbuj innej!")
                return false
            }
        }

        var temp = 1
        var j = 0
        for (i in 0..word.length - 1) {
            if (letter == word[i]) {
                letterFlag = 1
                while (j < i) {
                    temp += 3
                    j++
                }
                val text = StringBuilder(totalText).also { it.setCharAt(temp, letter) }
                totalText = text.toString()
            }
        }
        if (letterFlag == 1) {
            binding.word.text = totalText.toString()
            if(pop)
                popUp("Brawo! Zgadłeś spółgłoskę, kwota: ${moneyCache}$ ląduje na twoim koncie!")
            money += moneyCache
            binding.moneyAccount.text = "Stan konta: ${money}$"
            moneyCache = 0
            if (pop)
                (activity as MainActivity).phaseNumber = 3
            if(pop) {
                Handler().postDelayed(
                    { popUp("Spróbuj zgadnąć hasło lub oddaj turę przeciwnikowi!") },
                    2500
                )
            }
            return true
        }
        missedLetters += letter
        if(pop)
            popUp("Niestety nie udało się zgadnąć spółgłoski. Kwota: ${moneyCache}$ przepada!")
        moneyCache = 0
        if (pop) {
            (activity as MainActivity).phaseNumber = 3
            Handler().postDelayed(
                { popUp("Spróbuj zgadnąć hasło lub oddaj turę przeciwnikowi!") },
                2500
            )
        }
        return false
    }

    //function which implements letter buttons actions
    fun letterButtons() {
        val toast = Toast.makeText(this.context, "Zakręć kołem!", Toast.LENGTH_SHORT)
        val opposingToast = Toast.makeText(this.context, "Teraz jest tura przeciwnika!", Toast.LENGTH_SHORT)

        binding.LetterB.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('B',true)
                    connectedThread.write("B".toByteArray())
                    binding.LetterB.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterC.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('C',true)
                    connectedThread.write("C".toByteArray())
                    binding.LetterC.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterD.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('D',true)
                    connectedThread.write("D".toByteArray())
                    binding.LetterD.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterF.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('F',true)
                    connectedThread.write("F".toByteArray())
                    binding.LetterF.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterH.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('H',true)
                    connectedThread.write("H".toByteArray())
                    binding.LetterH.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterG.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('G',true)
                    connectedThread.write("G".toByteArray())
                    binding.LetterG.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterJ.setOnClickListener {
            if( (activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('J',true)
                    connectedThread.write("J".toByteArray())
                    binding.LetterJ.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterK.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('K',true)
                    connectedThread.write("K".toByteArray())
                    binding.LetterK.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterL.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('L',true)
                    connectedThread.write("L".toByteArray())
                    binding.LetterL.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterM.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('M',true)
                    connectedThread.write("M".toByteArray())
                    binding.LetterM.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterN.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('N',true)
                    connectedThread.write("N".toByteArray())
                    binding.LetterN.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterP.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('P',true)
                    connectedThread.write("P".toByteArray())
                    binding.LetterP.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterR.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('R',true)
                    connectedThread.write("R".toByteArray())
                    binding.LetterR.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterS.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('S',true)
                    connectedThread.write("S".toByteArray())
                    binding.LetterS.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterT.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('T',true)
                    connectedThread.write("T".toByteArray())
                    binding.LetterT.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterV.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('V',true)
                    connectedThread.write("V".toByteArray())
                    binding.LetterV.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterW.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('W',true)
                    connectedThread.write("W".toByteArray())
                    binding.LetterW.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterX.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('X',true)
                    connectedThread.write("X".toByteArray())
                    binding.LetterX.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
        binding.LetterZ.setOnClickListener {
            if((activity as MainActivity).phaseNumber != 4) {
                if ((activity as MainActivity).phaseNumber == 2) {
                    checkLetter('Z',true)
                    connectedThread.write("Z".toByteArray())
                    binding.LetterZ.isEnabled = false
                } else if ((activity as MainActivity).phaseNumber == 1) {
                    toast.show()
                }
            }else{
                opposingToast.show()
            }
        }
    }

    //function that makes popup screens
    fun popUp(text: String) {
        val intent = Intent(this.context, PopUpWindow::class.java)
        intent.putExtra("popuptext", text)
        intent.putExtra("darkstatusbar", false)
        startActivity(intent)
    }

    fun resetLetterButtonsColor() {
        binding.LetterB.isEnabled = true
        binding.LetterC.isEnabled = true
        binding.LetterD.isEnabled = true
        binding.LetterF.isEnabled = true
        binding.LetterG.isEnabled = true
        binding.LetterH.isEnabled = true
        binding.LetterJ.isEnabled = true
        binding.LetterK.isEnabled = true
        binding.LetterL.isEnabled = true
        binding.LetterM.isEnabled = true
        binding.LetterN.isEnabled = true
        binding.LetterP.isEnabled = true
        binding.LetterR.isEnabled = true
        binding.LetterS.isEnabled = true
        binding.LetterT.isEnabled = true
        binding.LetterV.isEnabled = true
        binding.LetterW.isEnabled = true
        binding.LetterX.isEnabled = true
        binding.LetterZ.isEnabled = true
    }

    val secondmHandler = @SuppressLint("HandlerLeak")
    object: Handler(){
        override fun handleMessage(msg: Message) {

            when(msg!!.what) {
                MESSAGE_WRITE -> {
                    val writeBuf = msg.obj as ByteArray
                    val writeMessage = String(writeBuf)
                    sendMessage = writeMessage
                }
                MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    val readMessage = String(readBuf, 0, msg.arg1)
                    receivedMessage = readMessage
                }
                MESSAGE_TOAST -> {
                }
            }
        }
    }

    inner class SecondConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

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
                    Log.d("BT Connection: ", "Input stream was disconnected", e)
                    break
                }
                // Send the obtained bytes to the UI activity.
                val readMsg = secondmHandler.obtainMessage(
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
                Log.e("BT Connection: ", "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = secondmHandler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                secondmHandler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = secondmHandler.obtainMessage(
                MESSAGE_WRITE, -1, -1, mmBuffer)
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e("BT Connection: ", "Could not close the connect socket", e)
            }
        }
    }

    inner class NextTurnThread(): Thread(){
        override fun run(){
            /*
            while(true) {
                while (true) {
                    if ((activity as MainActivity).phaseNumber == 1 && (activity as MainActivity).opponentLetters != "") {
                        popUp("Twoja tura, zakręć kołem fortuny!")
                        for (i in (activity as MainActivity).opponentLetters) {
                        }
                        break
                    }
                }


             */
                while (true) {
                    if ((activity as MainActivity).phaseNumber == 4) {
                        isRunning = true
                    }
                }
            /*
            }

             */
        }
    }
}