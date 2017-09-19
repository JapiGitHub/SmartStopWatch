package com.example.janne.smartstopwatch01

import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_round_timer.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import android.os.CountDownTimer
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.util.*


class RoundTimerActivity : AppCompatActivity() {

    lateinit var sb_RoundTimer_Time : SeekBar

    private var RoundLength : Int = 2

    private var hours = 0
    private var minutes = 0
    private var seconds = 0
    private var millis = 0
    private var aikaTekstiksi : Long = 0

    private var RoundHasStarted : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_round_timer)

        //seekbar
        sb_RoundTimer_Time = findViewById(R.id.sb_RoundTimer_Time) as SeekBar
        sb_RoundTimer_Time.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                RoundLength = sb_RoundTimer_Time.progress
                tv_RoundTime.text = "${sb_RoundTimer_Time.progress}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        butt_RoundTimerStart.onClick { StartRound() }

    }



    private fun StartRound() {

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
                        object : CountDownTimer(RoundLength.toLong() * 60 * 1000, 10) {
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




}
