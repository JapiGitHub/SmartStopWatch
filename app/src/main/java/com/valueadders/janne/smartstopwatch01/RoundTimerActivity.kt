package com.valueadders.janne.smartstopwatch01

import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_round_timer.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import android.os.CountDownTimer
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.longToast
import java.util.*


class RoundTimerActivity : AppCompatActivity() {

    lateinit var sb_RoundTimer_Time : SeekBar

    private var RoundLength : Int = 2

    var DownTimer: CountDownTimer? = null

    private var hours = 0
    private var minutes = 0
    private var seconds = 0
    private var millis = 0
    private var aikaTekstiksi : Long = 0
    private var PauseMillisLeft : Long = 0
    private var RoundLengthMillis : Long = 0
    private var RoundStartsIn : Long = 0

    private var RoundHasStarted : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // fullscreen setit
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_round_timer)
        supportActionBar!!.hide()

        // pitää portraittina!
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        MainLinearLayoutRoundTimer.requestFocus()

        butt_RoundTimerStart.onClick { StartButtonPress() }

    }




    private fun StartButtonPress() {

        when (butt_RoundTimerStart.text) {
            "Start"     -> startti()
            "Pause"     -> pause()
            "Continue"  -> jatka()
        }

        MainLinearLayoutRoundTimer.requestFocus()

/*        //jos napissa lukee Start -> tee normaalisti
        if (butt_RoundTimerStart.text == "Start") {
            butt_RoundTimerStart.text = "Pause"

            runOnUiThread { longToast("Starting in 5 sec!") }

            //jotta saadaan erän alkuun 5 sec
            var StartUpTimer = Timer()


            //schedule warmup 5 sec
            StartUpTimer.schedule(object : TimerTask() {
                override fun run() {

                    //Round start beep
                    val BoxingBell_ShortLoudEnd = MediaPlayer.create(applicationContext, R.raw.boxingbellshortloud)
                    BoxingBell_ShortLoudEnd.start()

                    runOnUiThread {
                        DownTimer = object : CountDownTimer(RoundLength.toLong() * 60 * 1000, 10) {
                            override fun onTick(millisUntilFinished: Long) {

                                aikaTekstiksi = millisUntilFinished
                                millis = (aikaTekstiksi.toInt() % 1000)
                                seconds = (aikaTekstiksi.toInt() / 1000) % 60
                                minutes = (aikaTekstiksi.toInt() / 60000) % 60
                                hours = (aikaTekstiksi.toInt() / 3600000) % 60
                                tv_RoundTimer.text = "$hours : $minutes : $seconds : $millis"
                            }

                            override fun onFinish() {
                                tv_RoundTimer.text = "ROUND END"
                                val BoxingBell_ShortLoudEnd = MediaPlayer.create(applicationContext, R.raw.boxingbellshortloud)
                                BoxingBell_ShortLoudEnd.start()
                                RoundHasStarted = false
                            }

                        }.start()
                    }

                }}, 5000)
            // tv_RoundTimer.postDelayed(Runnable { updateTime() }, 50)
        }

        if (butt_RoundTimerStart.text == "Pause") {
            //jos napissa on lukenut Pause
            runOnUiThread {

                DownTimer?.cancel()
                PauseMillisLeft = ((hours * 3600000) + (minutes * 60000) + (seconds * 1000) + (millis)).toLong()
                butt_RoundTimerStart.text = "Continue"
            }

        }

        if (butt_RoundTimerStart.text == "Continue") {
            runOnUiThread {
                DownTimer = object : CountDownTimer(PauseMillisLeft, 10) {
                    override fun onTick(millisUntilFinished: Long) {

                        aikaTekstiksi = millisUntilFinished
                        millis = (aikaTekstiksi.toInt() % 1000)
                        seconds = (aikaTekstiksi.toInt() / 1000) % 60
                        minutes = (aikaTekstiksi.toInt() / 60000) % 60
                        hours = (aikaTekstiksi.toInt() / 3600000) % 60
                        tv_RoundTimer.text = "$hours : $minutes : $seconds : $millis"
                    }

                    override fun onFinish() {
                        tv_RoundTimer.text = "ROUND END"
                        val BoxingBell_ShortLoudEnd = MediaPlayer.create(applicationContext, R.raw.boxingbellshortloud)
                        BoxingBell_ShortLoudEnd.start()
                        RoundHasStarted = false
                    }

                }.start()
            }
        }*/

    }

    private fun jatka() {
        runOnUiThread {
            butt_RoundTimerStart.backgroundResource = R.drawable.roundedbuttonsorange
            DownTimer = object : CountDownTimer(PauseMillisLeft, 10) {
                override fun onTick(millisUntilFinished: Long) {

                    aikaTekstiksi = millisUntilFinished
                    millis = (aikaTekstiksi.toInt() % 1000) / 100
                    seconds = (aikaTekstiksi.toInt() / 1000) % 60
                    minutes = (aikaTekstiksi.toInt() / 60000) % 60
                    hours = (aikaTekstiksi.toInt() / 3600000) % 60
                    //tv_RoundTimer.text = "$hours : $minutes : $seconds : $millis"

                    et_Hours_RoundTimer.setText(hours.toString(), TextView.BufferType.EDITABLE)
                    et_Minutes_RoundTimer.setText(minutes.toString(),TextView.BufferType.EDITABLE)
                    et_Seconds_RoundTimer.setText(seconds.toString(),TextView.BufferType.EDITABLE)
                    et_Millis_RoundTimer.setText(millis.toString(), TextView.BufferType.EDITABLE)
                }

                override fun onFinish() {
                    //tv_RoundTimer.text = "ROUND END"
                    val BoxingBell_ShortLoudEnd = MediaPlayer.create(applicationContext, R.raw.boxingbellshortloud)
                    BoxingBell_ShortLoudEnd.start()
                    RoundHasStarted = false
                    // laita tähän setit jotta voi heti aloittaa uuden jos haluaa?
                }

            }.start()

            butt_RoundTimerStart.text = "Pause"
        }
    }

    private fun pause() {
        //jos napissa on lukenut Pause
        runOnUiThread {

            butt_RoundTimerStart.backgroundResource = R.drawable.roundedbuttons

            DownTimer?.cancel()
            PauseMillisLeft = ((hours * 3600000) + (minutes * 60000) + (seconds * 1000) + (millis)).toLong()
            butt_RoundTimerStart.text = "Continue"
        }
    }

    private fun startti() {
        butt_RoundTimerStart.backgroundResource = R.drawable.roundedbuttonsorange
        RoundStartsIn = et_RoundStartsIn_RoundTimer.text.toString().toLong() * 1000

        et_RoundStartsIn_RoundTimer.visibility = View.GONE
        tv_infoRT1.visibility = View.GONE

        butt_RoundTimerStart.text = "Pause"


        RoundLengthMillis = (et_Hours_RoundTimer.text.toString().toLong() * 60 * 60 * 1000) + (et_Minutes_RoundTimer.text.toString().toLong() * 1000 * 60) + (et_Seconds_RoundTimer.text.toString().toLong() * 1000)

        runOnUiThread { longToast("Starting in ${RoundStartsIn / 1000} sec!") }

        //jotta saadaan erän alkuun 5 sec
        var StartUpTimer = Timer()


        //schedule warmup 5 sec
        StartUpTimer.schedule(object : TimerTask() {
            override fun run() {

                //Round start beep
                val BoxingBell_ShortLoudEnd = MediaPlayer.create(applicationContext, R.raw.boxingbellshortloud)
                BoxingBell_ShortLoudEnd.start()

                runOnUiThread {
                    DownTimer = object : CountDownTimer(RoundLengthMillis, 10) {
                        override fun onTick(millisUntilFinished: Long) {

                            aikaTekstiksi = millisUntilFinished
                            millis = (aikaTekstiksi.toInt() % 1000) / 100
                            seconds = (aikaTekstiksi.toInt() / 1000) % 60
                            minutes = (aikaTekstiksi.toInt() / 60000) % 60
                            hours = (aikaTekstiksi.toInt() / 3600000) % 60
                            //tv_RoundTimer.text = "$hours : $minutes : $seconds : $millis"

                            et_Hours_RoundTimer.setText(hours.toString(), TextView.BufferType.EDITABLE)
                            et_Minutes_RoundTimer.setText(minutes.toString(),TextView.BufferType.EDITABLE)
                            et_Seconds_RoundTimer.setText(seconds.toString(),TextView.BufferType.EDITABLE)
                            et_Millis_RoundTimer.setText(millis.toString(), TextView.BufferType.EDITABLE)
                        }

                        override fun onFinish() {
                            //tv_RoundTimer.text = "ROUND END"
                            val BoxingBell_ShortLoudEnd = MediaPlayer.create(applicationContext, R.raw.boxingbellshortloud)
                            BoxingBell_ShortLoudEnd.start()
                            RoundHasStarted = false
                        }

                    }.start()
                }

            }}, RoundStartsIn)
        // tv_RoundTimer.postDelayed(Runnable { updateTime() }, 50)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        startActivity(Intent(this@RoundTimerActivity, MainMenuActivity::class.java))
        finish()

    }


}
