package com.example.janne.smartstopwatch01

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import org.jetbrains.anko.button
import org.jetbrains.anko.editText
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast
import org.jetbrains.anko.verticalLayout
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenuActivity : AppCompatActivity() {

    // STOPWATCHia varten
    var startTime = System.currentTimeMillis()
    private var stopTime : Long = 0
    private var pauseTimeStart : Long = 0
    private var pauseTimeResult : Long = 0

    var aikaTekstiksi : Long = 0

    var seconds : Int = 0
    var minutes : Int = 0
    var hours : Int = 0
    var millis : Int = 0

    var stpWtchIsRunning : Boolean = false


    val REQUEST_RECORD_AUDIO_PERMISSION = 200


    // Permissions jatkuu onCreatesssa (activityCompat.request...)   ja   lopussa companion object (private val REQUEST_RECORD_AUDIO_PERMISSION = 200)
    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    //tähän onRequestPermissionResult
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ReactionActivity.REQUEST_RECORD_AUDIO_PERMISSION -> permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionToRecordAccepted) finish()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // fullscreen setit
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main_menu)
        supportActionBar!!.hide()

        //permissions
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)


        //buttons

        buttReaction.onClick                { startActivity(Intent(this@MainMenuActivity,ReactionActivity::class.java)) }
        buttReactionHistory.onClick         { startActivity(Intent(this@MainMenuActivity,ReactionHistoryActivity::class.java)) }
        buttCountREPs.onClick               { startActivity(Intent(this@MainMenuActivity,CounterActivity::class.java)) }
        buttCountHistory.onClick            { startActivity(Intent(this@MainMenuActivity,chronoTest::class.java)) }
        buttSettings.onClick                { startActivity(Intent(this@MainMenuActivity,RoundTimerActivity::class.java)) }

        butt_start.onClick { startStpWatch() }
        butt_stop.onClick { stopStpWatch() }
        butt_reset.onClick { Reset() }

        tv_BasicStopwatch.text = "00:00:00: 00"

    }


    fun startStpWatch() {
        stpWtchIsRunning = true
        println("KONSOLI   : stopwatch has been started")

        //tarkistaa onko vaan pausella vai onko mittari nollissa
        if (aikaTekstiksi > 1 ) {
            //mittari ei ole nollissa, eli on vaan pausella, jatketaan siis siitä mihin jäätiin
            //lopetetaan pause ja katotaan paljonko aikaa meni pausen aikana
            pauseTimeResult = System.currentTimeMillis() - pauseTimeStart
            println("KONSOLI   : pause   $pauseTimeResult")
        } else {
            //mittari nollissa, joten voi huoletta lähteä alusta liikkeelle
            startTime = System.currentTimeMillis()
        }

        tv_BasicStopwatch.postDelayed(Runnable { updateTextView() }, 50)
    }


    fun updateTimes() {

        aikaTekstiksi = System.currentTimeMillis() - (startTime + pauseTimeResult)

        millis = (aikaTekstiksi.toInt() % 1000)
        seconds = (aikaTekstiksi.toInt() / 1000) % 60
        minutes = (aikaTekstiksi.toInt() / 60000) % 60
        hours = (aikaTekstiksi.toInt() / 3600000) % 60
    }


    // https://stackoverflow.com/questions/36476691/variable-runnable-must-be-initialized
    private fun updateTextView() {

        updateTimes()

        if (stpWtchIsRunning) {
            runOnUiThread { tv_BasicStopwatch.text = "$hours:$minutes:$seconds : $millis" }
        }

        //jatkaa päivittämistä 50ms välein
        if (stpWtchIsRunning) {
            tv_BasicStopwatch.postDelayed( Runnable { updateTextView() }, 50)
        }
    }



    private fun stopStpWatch() {
        println("KONSOLI   : STOPPIA painettu")

        if (stpWtchIsRunning) {
            stopTime = System.currentTimeMillis()
        }

        //aloita pause
        if (pauseTimeResult > 0) {
            pauseTimeStart = System.currentTimeMillis() - pauseTimeResult
        } else {
            pauseTimeStart = System.currentTimeMillis()
        }

        stpWtchIsRunning = false

        println("KONSOLI   : aikaTekstiksi :  $aikaTekstiksi")

        millis = (aikaTekstiksi.toInt() % 1000)
        seconds = (aikaTekstiksi.toInt() / 1000) % 60
        minutes = (aikaTekstiksi.toInt() / 60000) % 60
        hours = (aikaTekstiksi.toInt() / 360000) % 60

        //aikaFormat00.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, millis)

        if (stpWtchIsRunning) { tv_BasicStopwatch.text = "$hours:$minutes:$seconds : $millis" }
        println("KONSOLI   : STOPPED ! $hours:$minutes:$seconds : $millis ")

    }

    private fun Reset() {

        runOnUiThread { tv_BasicStopwatch.text = "00:00:00 : 00" }

        stpWtchIsRunning = false
        startTime = 0
        stopTime = 0
        pauseTimeResult = 0
        pauseTimeStart = 0

        millis = 0
        seconds = 0
        minutes = 0
        hours = 0

        aikaTekstiksi = 0

        runOnUiThread { tv_BasicStopwatch.text = "00:00:00 : 00" }

    }








}