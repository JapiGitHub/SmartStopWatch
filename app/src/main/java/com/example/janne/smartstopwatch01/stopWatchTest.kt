package com.example.janne.smartstopwatch01

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.UiThread
import kotlinx.android.synthetic.main.activity_stop_watch_test.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast
import java.util.concurrent.TimeUnit.*



class stopWatchTest : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_watch_test)


        btn_stpW_Start.onClick        { startStpWatch() }
        btn_stpW_Stop.onClick        { stopStpWatch() }
        btn_StpW_Reset.onClick        { Reset() }

        tv_Timer.text = "00:00:00 : 00"

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

        tv_Timer.postDelayed(Runnable { updateTextView() }, 50)
    }


    fun updateTimes() {

        aikaTekstiksi = System.currentTimeMillis() - (startTime + pauseTimeResult)

        millis = (aikaTekstiksi.toInt() % 1000)
        seconds = (aikaTekstiksi.toInt() / 1000) % 60
        minutes = (aikaTekstiksi.toInt() / 60000) % 60
        hours = (aikaTekstiksi.toInt() / 360000) % 60
    }


    // https://stackoverflow.com/questions/36476691/variable-runnable-must-be-initialized
    private fun updateTextView() {

        updateTimes()

        if (stpWtchIsRunning) {
            runOnUiThread { tv_Timer.text = "$hours:$minutes:$seconds : $millis" }
        }

        //jatkaa päivittämistä 50ms välein
        if (stpWtchIsRunning) {
            tv_Timer.postDelayed( Runnable { updateTextView() }, 50)
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

        if (stpWtchIsRunning) { tv_Timer.text = "$hours:$minutes:$seconds : $millis" }
        println("KONSOLI   : STOPPED ! $hours:$minutes:$seconds : $millis ")

    }

    private fun Reset() {

        runOnUiThread { tv_Timer.text = "00:00:00 : 00" }

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

        runOnUiThread { tv_Timer.text = "00:00:00 : 00" }

    }

}





