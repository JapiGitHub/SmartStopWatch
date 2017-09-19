package com.example.janne.smartstopwatch01

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.series.PointsGraphSeries
import kotlinx.android.synthetic.main.activity_counter.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.PrintWriter


class CounterActivity : AppCompatActivity() {


    private var count  : Int = 0
    private var audioRec: AudioRecord? = null
    private var bufferSize: Int = 0
    private var ViimeisinMaxAmplitude = 0.0
    private var thread: Thread? = null
    private var HowLongSinceLastHit = 0
    private var WaitBeforeStartCountingAgain = 3

    private var maxAmpEver = 0

    private var threshold = 1500
    var thresholdDouble : Double = threshold.toDouble()
    private var thresholdStrIntDUMP : String = "61"
    lateinit var etThreshold : EditText

    //graph
    lateinit var series: LineGraphSeries<DataPoint>
    lateinit var seriesHITs: PointsGraphSeries<DataPoint>
    lateinit var thresholdLineinGraph : LineGraphSeries<DataPoint>
    var Xline : Double = 0.0
    var Yline : Double = 0.0
    var x : Double = 0.0
    var y : Double = 0.0


    //seekbar for wait next hit
    lateinit var sb_waitNextHit : SeekBar
    lateinit var sb_Threshold : SeekBar

    //          PERMISSIONS                         ------------------------------------------
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



    //      ON CREATE       -------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("KONSOLI   :  OnCreate alkaa ------------------------------------------------------------ ")

        //permissions
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        // fullscreen setit
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_counter)
        supportActionBar!!.hide()

        for (rate in intArrayOf(44100, 22050, 11025, 16000, 8000)) {  // add the rates you wish to check against
            bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            if (bufferSize > 0) {
                // buffer size is valid, Sample rate supported
                sampleRate = rate
                println("KONSOLI buffersize toimii : $bufferSize   sampleRate : $rate")
            }
        }


        sb_Threshold = findViewById(R.id.sb_Threshold) as SeekBar
        sb_Threshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                threshold = sb_Threshold.progress
                thresholdDouble = threshold.toDouble()
                println("KONSOLI   :  threshold set to : $threshold")
                etThreshold.setText("${sb_Threshold.progress}", TextView.BufferType.EDITABLE)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sb_waitNextHit = findViewById(R.id.sb_waitNextHit) as SeekBar
        sb_waitNextHit.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                println("KONSOLI   : seekbar : ${sb_waitNextHit.progress}")
                tv_info.text = "seekbar : ${sb_waitNextHit.progress}"
                WaitBeforeStartCountingAgain = sb_waitNextHit.progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

        //napit
        buttReactionCalibration.onClick     { calibrate() }
        buttReactionStart.onClick           { aloita() }
        butt_Reset.onClick                  { ResetCount() }



        //graph <
        var graphview : GraphView = findViewById(R.id.graph) as GraphView

        series = LineGraphSeries<DataPoint>()
        seriesHITs = PointsGraphSeries<DataPoint>()
        thresholdLineinGraph = LineGraphSeries<DataPoint>()

        seriesHITs.color = Color.RED
        seriesHITs.size = 60f

        series.thickness = 15

        graph.gridLabelRenderer.isVerticalLabelsVisible = false
        graph.gridLabelRenderer.isHorizontalLabelsVisible = false
        graph.gridLabelRenderer.isHighlightZeroLines = false

        graph.gridLabelRenderer.gridColor = 0

        graphview.getViewport().setScrollable(true)

        // set manual Y bounds
        graph.getViewport().setYAxisBoundsManual(true)
        graph.getViewport().setMinY(0.0)
        graph.getViewport().setMaxY(32000.0)

        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true)
        graph.getViewport().setMinX(0.0)
        graph.getViewport().setMaxX(25.0)



        /*
        for (i in 0..400)
        {
            x += 0.1
            y = 5.0
            series.appendData(DataPoint(x,y), true, 500)
        }

        graphview.addSeries(series)
        */
        // graph loppuu >

    }


    private fun ResetCount() {
        count = 0
        runOnUiThread { tv_Count.text = "0" }
    }


    //onResume alkaa aina onCreaten (tai onStartin) jälkeen ja lisäksi onPausen jälkeen.
    override fun onResume() {
        super.onResume()
/*
        // initializing audioRec
        println("KONSOLI   : initializing AudioRec")                            //  TYÖ  jostain syystä tää tulee 2x
        audioRec = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize)

        audioRec!!.startRecording()

        thread = Thread(Runnable {
            while (thread != null && !thread!!.isInterrupted) {
                //sleep  for SAMPLE_DELAY tutkimisen välissä
                try {
                    Thread.sleep(SAMPLE_DELAY.toLong())
                } catch (ie: InterruptedException) {
                    ie.printStackTrace()
                }

                MaxAmplitudeTest()      //maxAmp testi

                when {
                    ViimeisinMaxAmplitude > 60    -> hitDetected()
                    ViimeisinMaxAmplitude == 0.0  -> println("KONSOLI   : total:${Thread.activeCount()}  this.ID:${Thread.currentThread().id}  $SAMPLE_DELAY ms,  ${ViimeisinMaxAmplitude.toInt()}   default")
                    else                          -> println("KONSOLI   : total:${Thread.activeCount()}  this.ID:${Thread.currentThread().id}  $SAMPLE_DELAY ms,  ${ViimeisinMaxAmplitude.toInt()} -")
                }
            }
        })

        println("KONSOLI   : total:${Thread.activeCount()}  this.ID:${Thread.currentThread().id}  THREAD LOPPUI     ***      ***")
        thread!!.start()
        */
    }

    private fun hitDetected() {
        if (HowLongSinceLastHit > WaitBeforeStartCountingAgain) {
            //println("KONSOLI   : total:${Thread.activeCount()}  this.ID:${Thread.currentThread().id}  $SAMPLE_DELAY ms,  ${ViimeisinMaxAmplitude.toInt()} BING BING !!!!!")
            count = count + 1
            println("KONSOLI   : KOUNTTI  $count")

            runOnUiThread { tvHistory.text = "${ViimeisinMaxAmplitude.toInt()} \n ${tvHistory.text}" }

            //jottei HIT punainen pallo mene chartista yli...
            if (ViimeisinMaxAmplitude > 200) {
                seriesHITs.appendData(DataPoint(x, 200.0), true, 25)
            } else {
                seriesHITs.appendData(DataPoint(x, ViimeisinMaxAmplitude), true, 25)
            }

            runOnUiThread {
                tv_Count.text = "$count"
            }
            HowLongSinceLastHit = 0
        }
        else {
            println("KONSOLI   : jälkitärinää vain.. NOT count")
        }

    }



    private fun calibrate() {



        println("KONSOLI   : calibrate funktio alkaa ...")
        // tähän looppi recordaamaan esim 20 kierrosta ja sitten katsomaan loudest noise.
        //threshold = loudest noise * 0.8


        runOnUiThread { tv_info.text = "Do your activity in next 5 seconds and I know what to listen for..." }

        // initializing audioRec
        println("KONSOLI   : initializing AudioRec")                            //  TYÖ  jostain syystä tää tulee 2x
        audioRec = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize)

        audioRec!!.startRecording()

        //tee 20 kierrosta kalibroimista varten ja ota sieltä isoin AMP


        thread = Thread(Runnable {
            while (thread != null && !thread!!.isInterrupted) {
                //sleep  for SAMPLE_DELAY tutkimisen välissä
                try {
                    Thread.sleep(SAMPLE_DELAY.toLong())
                } catch (ie: InterruptedException) {
                    ie.printStackTrace()
                }
                HowLongSinceLastHit += 1
                MaxAmplitudeTest()      //maxAmp testi

                // tallenna ViimeisinMaxAmplitude taulukkoon ja vertaile for loopin jälkeen k

                if (ViimeisinMaxAmplitude > threshold) {
                    hitDetected()
                }

                if (ViimeisinMaxAmplitude > maxAmpEver) {
                    maxAmpEver = ViimeisinMaxAmplitude.toInt()
                    runOnUiThread { tv_info.text ="max : $maxAmpEver" }
                }



                //TYÖ
                x = x + 1
                series.appendData(DataPoint(x,ViimeisinMaxAmplitude), true, 25)

                Yline = thresholdDouble
                thresholdLineinGraph.appendData(DataPoint(x, Yline), true, 25)


                //tämä tarvii olla jottei kaadu heti 50x mittauksen jälkeen...
                if (x.toInt() % 25 == 1) {
                    graph.removeAllSeries()
                    graph.addSeries(series)
                    graph.addSeries(seriesHITs)
                    graph.addSeries(thresholdLineinGraph)
                }

                when {
                    (ViimeisinMaxAmplitude < 10)                                      -> println("KONSOLI   :max amp on 0x   : ${ViimeisinMaxAmplitude.toInt()}  *")
                    ((ViimeisinMaxAmplitude >=10) && (ViimeisinMaxAmplitude < 20))    -> println("KONSOLI   :max amp on 1x   : ${ViimeisinMaxAmplitude.toInt()}  **")
                    ((ViimeisinMaxAmplitude >=20) && (ViimeisinMaxAmplitude < 30))    -> println("KONSOLI   :max amp on 2x   : ${ViimeisinMaxAmplitude.toInt()}  ***")
                    ((ViimeisinMaxAmplitude >=30) && (ViimeisinMaxAmplitude < 40))    -> println("KONSOLI   :max amp on 3x   : ${ViimeisinMaxAmplitude.toInt()}  ****")
                    ((ViimeisinMaxAmplitude >=40) && (ViimeisinMaxAmplitude < 50))    -> println("KONSOLI   :max amp on 4x   : ${ViimeisinMaxAmplitude.toInt()}  *****")
                    ((ViimeisinMaxAmplitude >=50) && (ViimeisinMaxAmplitude < 60))    -> println("KONSOLI   :max amp on 5x   : ${ViimeisinMaxAmplitude.toInt()}  ******")
                    ((ViimeisinMaxAmplitude >=60) && (ViimeisinMaxAmplitude < 70))    -> println("KONSOLI   :max amp on 6x   : ${ViimeisinMaxAmplitude.toInt()}  *******")
                    ((ViimeisinMaxAmplitude >=70) && (ViimeisinMaxAmplitude < 80))    -> println("KONSOLI   :max amp on 7x   : ${ViimeisinMaxAmplitude.toInt()}  ********")
                    ((ViimeisinMaxAmplitude >=80) && (ViimeisinMaxAmplitude < 90))    -> println("KONSOLI   :max amp on 8x   : ${ViimeisinMaxAmplitude.toInt()}  *********")
                    ((ViimeisinMaxAmplitude >=90) && (ViimeisinMaxAmplitude < 100))   -> println("KONSOLI   :max amp on 9x   : ${ViimeisinMaxAmplitude.toInt()}  **********")
                    ((ViimeisinMaxAmplitude >=100) && (ViimeisinMaxAmplitude < 110))  -> println("KONSOLI   :max amp on 10x  : ${ViimeisinMaxAmplitude.toInt()}  ***********")
                    ((ViimeisinMaxAmplitude >=110) && (ViimeisinMaxAmplitude < 120))  -> println("KONSOLI   :max amp on 11x  : ${ViimeisinMaxAmplitude.toInt()}  MAXXXXXXXXXXXXX")
                //ViimeisinMaxAmplitude > 60      -> hitDetected()
                //ViimeisinMaxAmplitude == 0.0    -> println("KONSOLI   : total:${Thread.activeCount()}  this.ID:${Thread.currentThread().id}  $SAMPLE_DELAY ms,  ${ViimeisinMaxAmplitude.toInt()}   default")
                    else                            -> println("KONSOLI   : total:${Thread.activeCount()}  this.ID:${Thread.currentThread().id}  $SAMPLE_DELAY ms,  ${ViimeisinMaxAmplitude.toInt()} -?????")
                }
            }
        })


        println("KONSOLI   : total:${Thread.activeCount()}  this.ID:${Thread.currentThread().id}  THREAD LOPPUI     ***      ***")
        thread!!.start()






    }


    private fun aloita() {


        try {
            val writer = PrintWriter(getFilesDir().getPath())  // java.io.PrintWriter
            writer.append("terve")
            writer.close()
        }
        catch (e: FileNotFoundException) {
            println("KONSOLI   :  file not found")
        }
        //val writer = PrintWriter("/data/user/0/com.example.janne.smartstopwatch01/files/file.txt")  // java.io.PrintWriter



/*
        val filename = "myfile"
        val string = "Hello world!"
        val outputStream: FileOutputStream

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE)
            outputStream.write(string.toByteArray())
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
*/

        // var AppDir : File? = getFilesDir()

    //println("KONSOLI   : ${AppDir.toString()}")

/*
        File("${getFilesDir()}/somefile.txt").printWriter().use { out ->
                out.println("terve mieheen")

        }


        //val outString: String = "Kotlination\nBe Kotlineer - Be Simple - Be Connective."
        //File("${AppDir}/kotlination1.txt").printWriter().use { out -> out.println(outString) }

        File(getFilesDir(), "filename.txt")
        File("${getFilesDir()}", "somefile.txt").printWriter().use { out ->
            out.println("terve mieheen2222")

        }


//        mFileName = externalCacheDir.absolutePath
//        mFileName += "/audiorecordtest.3gp"
*/
        println("KONSOLI   : ${filesDir.path}")         // näissä kahdessa
        println("KONSOLI   : ${filesDir}")              // ei ole mitään eroa...
        println("KONSOLI   :  file write meni läpi...   onko noissa ylemmissä mitään eroa")

    }


    // MAX AMPLITUDE tai äänen kovuus testi

    private fun MaxAmplitudeTest() {

        try {
            val buffer = ShortArray(bufferSize)         // short minimum -32,768 and a maximum 32,767

            var bufferReadResult = 1

            if (audioRec != null) {

                // Sense the voice...
                bufferReadResult = audioRec!!.read(buffer, 0, bufferSize)           // bufferReadResult = 640 eli sama kuin bufferSize
                //println("bKONSOLI   :  BufferReadResult : $bufferReadResult / $bufferSize")
                var sumLevel = 0.0

                //TYÖ miksi toDouble? miksei short kävisi?
                //sumLeveliin lisätään jokainen buffer arrayn arvo 0...640 (bufferReadResult)
                for (i in 0..bufferReadResult - 1) {
                    //println("cKONSOLI   : $i buffer : ${Math.abs(buffer[i].toInt())}")      //tämä ei näytä ainakaan miinuksia
                    sumLevel += buffer[i].toDouble()    //tämähän plussaa nyt myös miinus äänet!
                }

                ViimeisinMaxAmplitude = buffer.max()!!.toDouble()
                println("KONSOLI   : MaxAMP  :  ${ViimeisinMaxAmplitude.toInt()}")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun MaxAmplitudeTestBackUp() {

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
                ViimeisinMaxAmplitude = Math.abs(sumLevel / bufferReadResult)   // abs = absolute value, eli ei negatiivisia
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    // menee on pauseen myös jos laitetta kääntää!
    override fun onPause() {
        super.onPause()
        println("KONSOLI   : onPause ...")
        if (thread != null) {
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


    }


    //An object declaration inside a class can be marked with the companion keyword
    companion object {

        private var sampleRate = 8000
        private val SAMPLE_DELAY = 90

        //private val LOG_TAG = "AudioRecordTest"
        val REQUEST_RECORD_AUDIO_PERMISSION = 200
        //private var mFileName: String? = null


    }


}
