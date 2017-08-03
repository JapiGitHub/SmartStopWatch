package com.example.janne.smartstopwatch01

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView


class ReactionActivity : AppCompatActivity() {



    private var audioRec: AudioRecord? = null
    private var bufferSize: Int = 0
    private var ViimeisinMaxAmplitude = 0.0
    private var thread: Thread? = null
    //private var mouthImage: ImageView? = null




    // Permissions jatkuu onCreatesssa (activityCompat.request...)   ja   lopussa companion object (private val REQUEST_RECORD_AUDIO_PERMISSION = 200)
    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    //tähän onRequestPermissionResult
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionToRecordAccepted) finish()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //permissions
       ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)


        for (rate in intArrayOf(44100, 22050, 11025, 16000, 8000)) {  // add the rates you wish to check against
            bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            if (bufferSize > 0) {
                // buffer size is valid, Sample rate supported
                sampleRate = rate
                println("KONSOLI buffersize toimii : $bufferSize   sampleRate : $rate")
            }
        }


        // fullscreen setit
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_reaction)
        supportActionBar!!.hide()


    }




    override fun onResume() {
        super.onResume()

        // initializing audioRec
        println("KONSOLI   : initializing AudioRec")
        audioRec = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize)

        audioRec!!.startRecording()
        thread = Thread(Runnable {
            while (thread != null && !thread!!.isInterrupted) {
                //Let's make the thread sleep for a the approximate sampling time
                try {
                    Thread.sleep(SAMPLE_DELAY.toLong())
                } catch (ie: InterruptedException) {
                    ie.printStackTrace()
                }

                MaxAmplitudeTest()//After this call we can get the last value assigned to the maxAmp variable

                when {
                    ViimeisinMaxAmplitude > 60  -> println("KONSOLI   : $SAMPLE_DELAY ms,  ${ViimeisinMaxAmplitude.toInt()} +++++++")
                    else                        -> println("KONSOLI   : $SAMPLE_DELAY ms,  ${ViimeisinMaxAmplitude.toInt()} -")
                }

                /*
                runOnUiThread {
                    if (ViimeisinMaxAmplitude > 0 && ViimeisinMaxAmplitude <= 50) {
                        mouthImage!!.setImageResource(R.drawable.mouth4)
                    } else if (ViimeisinMaxAmplitude > 50 && ViimeisinMaxAmplitude <= 100) {
                        mouthImage!!.setImageResource(R.drawable.mouth3)
                    } else if (ViimeisinMaxAmplitude > 100 && ViimeisinMaxAmplitude <= 170) {
                        mouthImage!!.setImageResource(R.drawable.mouth2)
                    }
                    if (ViimeisinMaxAmplitude > 170) {
                        mouthImage!!.setImageResource(R.drawable.mouth1)
                    }
                    println("KONSOLI   : maxAmp  =  $ViimeisinMaxAmplitude")
                }
                */
            }
        })
        thread!!.start()
    }


    // MAX AMPLITUDE tai äänen kovuus testi
    private fun MaxAmplitudeTest() {

        try {
            val buffer = ShortArray(bufferSize)

            var bufferReadResult = 1

            if (audioRec != null) {

                // Sense the voice...
                bufferReadResult = audioRec!!.read(buffer, 0, bufferSize)
                var sumLevel = 0.0
                for (i in 0..bufferReadResult - 1) {
                    sumLevel += buffer[i].toDouble()
                }
                ViimeisinMaxAmplitude = Math.abs(sumLevel / bufferReadResult)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onPause() {
        super.onPause()
        thread!!.interrupt()
        thread = null
        try {
            if (audioRec != null) {
                audioRec!!.stop()
                audioRec!!.release()
                audioRec = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    //työ voiko tän vaan poistaa? mikä tää REQUEST_RECORC_PERM...  ??
    //An object declaration inside a class can be marked with the companion keyword
    companion object {

        private var sampleRate = 8000
        private val SAMPLE_DELAY = 75

        private val LOG_TAG = "AudioRecordTest"
        val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private var mFileName: String? = null


    }


}
