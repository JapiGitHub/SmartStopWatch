package com.example.janne.smartstopwatch01

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import org.jetbrains.anko.sdk25.coroutines.onClick
import kotlinx.android.synthetic.main.activity_main_menu.*
import org.jetbrains.anko.*

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

        // pitää portraittina!
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        //permissions
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        //AdMob
            if (Constants.type === Constants.Type.FREE) {
                MobileAds.initialize(this, "ca-app-pub-1077699782732928~8604184577")
                var mAdView = findViewById<AdView>(R.id.adViewTestii)
                val adRequesti = AdRequest.Builder().build()
                mAdView.loadAd(adRequesti)
            }

        //buttons

        buttReaction.onClick                {
            buttReaction.backgroundResource = R.drawable.roundedbuttonsorange
            startActivity(Intent(this@MainMenuActivity,ReactionActivity::class.java))
            }

        buttReactionHistory.onClick         {
            buttReactionHistory.backgroundResource = R.drawable.roundedbuttonsorange
            if (Constants.type === Constants.Type.FREE)
            {
                runOnUiThread { toast("Buy the PRO version to see your history") }
            } else {
                startActivity(Intent(this@MainMenuActivity,ReactionHistoryActivity::class.java))
            }
        }

        buttCountREPs.onClick               {
            buttCountREPs.backgroundResource = R.drawable.roundedbuttonsorange
            startActivity(Intent(this@MainMenuActivity,CounterActivity::class.java))
        }

        buttCountHistory.onClick            {
            buttCountHistory.backgroundResource = R.drawable.roundedbuttonsorange
            if (Constants.type === Constants.Type.FREE)
            {
                runOnUiThread { toast("Buy the PRO version to see your history") }
            } else {
                startActivity(Intent(this@MainMenuActivity,CountHistoryActivity::class.java))
            }
        }

        buttRoundTimer.onClick                {
            buttRoundTimer.backgroundResource = R.drawable.roundedbuttonsorange
            startActivity(Intent(this@MainMenuActivity,RoundTimerActivity::class.java)) }

        butt_start.onClick {
            butt_start.backgroundResource = R.drawable.roundedbuttonsorange
            if (butt_start.text == "PAUSE") {
                butt_start.backgroundResource = R.drawable.roundedbuttons
            }
            startStpWatch()

        }
        //butt_stop.onClick { stopStpWatch() }
        butt_reset.onClick {
            Reset()
            butt_start.backgroundResource = R.drawable.roundedbuttons
        }

        tv_BasicStopwatch.text = "0:0:0:0"
    }


    private fun startStpWatch() {

        if (butt_start.text == "PAUSE") {
            stopStpWatch()
            runOnUiThread { butt_start.backgroundResource = R.drawable.roundedbuttons }
        }
            else {
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

            butt_start.text = "PAUSE"
        }
    }


    private fun updateTimes() {

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
            runOnUiThread { tv_BasicStopwatch.text = "$hours:$minutes:$seconds:${millis/100}" }
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

        butt_start.text = "START"
    }

    private fun Reset() {

        runOnUiThread { tv_BasicStopwatch.text = "00:00:00:000" }

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

        runOnUiThread {
            tv_BasicStopwatch.text = "0:0:0:0"
            butt_start.text = "START"
        }
    }








}