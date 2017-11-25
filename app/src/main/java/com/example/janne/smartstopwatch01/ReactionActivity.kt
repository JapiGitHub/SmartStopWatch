package com.example.janne.smartstopwatch01

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.NumberPicker
import android.widget.SeekBar
import android.widget.TextView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.jjoe64.graphview.series.PointsGraphSeries
import kotlinx.android.synthetic.main.activity_reaction_v2.*
import me.toptas.fancyshowcase.FancyShowCaseView
import org.jetbrains.anko.longToast
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onFocusChange
import org.jetbrains.anko.sdk25.coroutines.onTouch
import org.jetbrains.anko.toast
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*


class ReactionActivity : AppCompatActivity() {


    private var count  : Int = 0
    private var audioRec: AudioRecord? = null
    private var bufferSize: Int = 0
    private var ViimeisinMaxAmplitude = 0.0
    private var thread: Thread? = null
    private var CalibrationThread : Thread? = null
    private var CalibrationInProgress : Boolean = true
    private var HowLongSinceLastHit = 0
    private var WaitBeforeStartCountingAgain = 3

    //private var hasRoundStarted : Boolean = false
    private var RoundEnding : Boolean = false

    private var threshold = 17000
    private var thresholdDouble : Double = threshold.toDouble()
    //private var thresholdStrIntDUMP : String = "61"

    //graph
    lateinit private var series: LineGraphSeries<DataPoint>
    lateinit private var seriesHITs: PointsGraphSeries<DataPoint>
    lateinit private var seriesHITsShadow: PointsGraphSeries<DataPoint>
    lateinit private var thresholdLineinGraph : LineGraphSeries<DataPoint>
    lateinit private var seriesReaction: LineGraphSeries<DataPoint>
    lateinit private var seriesHardReset: PointsGraphSeries<DataPoint>
    private var Yline : Double = 0.0
    private var x : Double = 0.0
    private var xReaction : Double = 0.0


    private var RoundStartsIn = 3000
    private var RoundLength : Int = 2
    private var RoundLengthMillis : Long = 0
    private var hours = 0
    private var minutes = 0
    private var seconds = 0
    private var millis = 0
    private var aikaTekstiksi : Long = 0
    private var RoundHasStarted : Boolean = false

    //seekbars
    lateinit var sbThreshold: SeekBar

    lateinit var NP_Minutes : NumberPicker
    lateinit var NP_Hours : NumberPicker
    lateinit var NP_Seconds : NumberPicker

    //random
    private var satunnaisGeneraattori = Random()
    private var RandomMin : Int = 1000
    private var RandomMax : Int = 5000
    //var randomSeku = satunnaisGeneraattori.nextInt(RandomMax) + RandomMin              //1000 ... 5000 random   1 ... 5 sekunttia
    //var randomSekuLong : Long = randomSeku.toLong()                           // .schedule tarvii Longin

    //var sekunttiKellottaja = Timer()

    private var BeepToHitDetectedTIMEstart = 0
    private var ReactionResultTIME = 0
    //var arrayReactionResult : MutableList<Int>? = mutableListOf(1)
    private var arrayReactionResult = mutableListOf<Int>()


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
        setContentView(R.layout.activity_reaction_v2)
        supportActionBar!!.hide()

        // pitää portraittina!
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        MainLinearLayout.requestFocus()

        tv_Wait.visibility = View.GONE

        //buffersize determination
        for (rate in intArrayOf(44100, 22050, 11025, 16000, 8000)) {  // add the rates you wish to check against
            bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            if (bufferSize > 0) {
                // buffer size is valid, Sample rate supported
                sampleRate = rate
                println("KONSOLI buffersize toimii : $bufferSize   sampleRate : $rate")
            }
        }


        // SEEKBARit
        sbThreshold = findViewById<SeekBar>(R.id.sb_Threshold)
        sbThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                threshold = sbThreshold.progress
                thresholdDouble = threshold.toDouble()
                println("KONSOLI   :  threshold set to : $threshold")

            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (count == 0) {  runOnUiThread { toast("Good, now push START")  }  }
            }
        })


        // NAPIT
        btn_ResetReaction.onClick        { resetProgress() }
        btn_StartReaction.onClick              { aloita() }


        // GRAPHit  <

        //var graphview : GraphView = findViewById(R.id.graph) as GraphView        vanha SDK 25
        var graphview = findViewById<GraphView>(R.id.graph)
        //var HistoryGraph : GraphView = findViewById(R.id.historyGraph) as GraphView

        //ääni graafit
            series = LineGraphSeries<DataPoint>()
            seriesHITs = PointsGraphSeries<DataPoint>()
                seriesHITsShadow = PointsGraphSeries<DataPoint>()
            thresholdLineinGraph = LineGraphSeries<DataPoint>()
            seriesHardReset = PointsGraphSeries<DataPoint>()


            //threshold line   #a1a193      btw. se tummempi väri on #5b605f
            val ThresholdColorOrange = Color.argb(255,255,102,0)
            val OrangeShadow = Color.argb(140,255,161,99)
            thresholdLineinGraph.color = ThresholdColorOrange
            thresholdLineinGraph.thickness = 7

            seriesHardReset.color = ThresholdColorOrange
            seriesHITs.color = ThresholdColorOrange
                seriesHITsShadow.color = OrangeShadow
            seriesHITs.size = 20f
                seriesHITsShadow.size = 50f
            seriesHardReset.size = 40f

            series.thickness = 2

            series.color = Color.TRANSPARENT
            series.isDrawBackground = true
            val graphColorBluish = Color.argb(200, 161, 161, 147)
            series.backgroundColor = graphColorBluish






            graph.gridLabelRenderer.isVerticalLabelsVisible = false
            graph.gridLabelRenderer.isHorizontalLabelsVisible = false
            graph.gridLabelRenderer.isHighlightZeroLines = false

            graph.gridLabelRenderer.gridColor = 0

            graphview.getViewport().setScrollable(true)

            // set manual Y bounds
            graph.getViewport().setYAxisBoundsManual(true)
            graph.getViewport().setMinY(0.0)
            graphview.getViewport().setMaxY(32000.0)

            // set manual X bounds
            graph.getViewport().setXAxisBoundsManual(true)
            graph.getViewport().setMinX(0.0)
            graph.getViewport().setMaxX(50.0)

            //reaktio graafi
            seriesReaction = LineGraphSeries<DataPoint>()

/*            HistoryGraph.getViewport().setScrollable(true)
            HistoryGraph.getViewport().setXAxisBoundsManual(true)
            HistoryGraph.getViewport().setYAxisBoundsManual(true)
            HistoryGraph.getViewport().setMaxX(50.0)
            HistoryGraph.getViewport().setMinX(0.0)
            HistoryGraph.getViewport().setMaxY(3000.0)
            HistoryGraph.getViewport().setMinY(0.0)
            HistoryGraph.gridLabelRenderer.isHorizontalLabelsVisible = false*/

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


        // ALOITTAA kalibrointia varten olevan graafin  eli se graafi mikä alkaa piirtämään heti viivaa
        audioRec = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize)

        audioRec!!.startRecording()


        println("KONSOLI   : Calibration Thread alkamassa")
        CalibrationThread = Thread(Runnable {
            while (CalibrationThread != null && !CalibrationThread!!.isInterrupted && CalibrationInProgress == true) {

               //sleep  for SAMPLE_DELAY tutkimisen välissä
                try {
                    Thread.sleep(SAMPLE_DELAY.toLong())
                } catch (ie: InterruptedException) {
                    ie.printStackTrace()
                }

                MaxAmplitudeTest()      //maxAmp testi



                //pakko olla runonUIthread, muuten tulee  ConcurrentModificationException
                runOnUiThread {

                    x = x + 1

                    series.appendData(DataPoint(x, ViimeisinMaxAmplitude), true, 50)
println("KONSOLI   : viimeisin max AMP   :   $ViimeisinMaxAmplitude")
                    Yline = thresholdDouble
                    thresholdLineinGraph.appendData(DataPoint(x, Yline), true, 50)


                    //tämä tarvii olla jottei kaadu heti 50x mittauksen jälkeen...

                        graph.removeAllSeries()
                        graph.addSeries(series)
                        graph.addSeries(seriesHITs)
                            graph.addSeries(seriesHITsShadow)
                        graph.addSeries(thresholdLineinGraph)

/*                        if (x.toInt() % 100 == 1) {
                            println("KONSOLI   : ressetDATA")
                            println("KONSOLI   : ressetDATA")

                            series.resetData(arrayOf(DataPoint(x, 1.0)))
                            thresholdLineinGraph.resetData(arrayOf(DataPoint(x,thresholdDouble)))

                        }*/








                }
                //println("KONSOLI   :   TOTAL THREADS :  ${Thread.activeCount()} ")

            }})

        //runOnUiThread { longToast("Set THRESHOLD and push START" )}

                CalibrationThread!!.start()

       FancyShowCaseView.Builder(this)
                .focusOn(sb_Threshold)
                .title("App makes a BEEP and then starts counting your reaction time. \n \n Just set sound threshold so app knows how loud sounds count for!")
                .titleStyle(R.style.fancyshowcasestyle, Gravity.TOP or Gravity.CENTER)
                //.showOnce("fancy1")
                .build()
                .show()

        /*FancyShowCaseView.Builder(this)
                .focusOn(sb_Threshold)
                .title("set Threhold and push start")
                .showOnce("fancy1")
                .build()
                .show()*/

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


/*    private fun hitDetected() {
        if (HowLongSinceLastHit > WaitBeforeStartCountingAgain) {
            //println("KONSOLI   : total:${Thread.activeCount()}  this.ID:${Thread.currentThread().id}  $SAMPLE_DELAY ms,  ${ViimeisinMaxAmplitude.toInt()} BING BING !!!!!")
            count = count + 1
            println("KONSOLI   : KOUNTTI  $count")

            //
            ReactionResultTIME = BeepToHitDetectedTIMEstart - System.currentTimeMillis().toInt()
            println("KONSOLI   :   varsinainen AIKA :    $ReactionResultTIME")

            //jottei HIT punainen pallo mene chartista yli...
            if (ViimeisinMaxAmplitude > 200) {
                seriesHITs.appendData(DataPoint(x, 200.0), true, 50)
            } else {
                seriesHITs.appendData(DataPoint(x, ViimeisinMaxAmplitude), true, 50)
            }

            runOnUiThread {
                tv_Count.text = "counttia  :  $count"
            }
            HowLongSinceLastHit = 0
        }
        else {
            println("KONSOLI   : jälkitärinää vain.. NOT count")
        }

    }*/


    private fun resetProgress() {
        RoundEnding = true

        //ei lopeta threadia :(
        thread!!.interrupt()

        count = 0

        CalibrationInProgress = false

        seriesHITs = seriesHardReset
        //historyGraph.removeAllSeries()

        arrayReactionResult!!.clear()

        runOnUiThread { tv_Count.text = "Count : 0" }

// tähän pitää saada seriesHITS reset!

        CalibrationThread?.interrupt()

        RoundEnding = false

        println("KONSOLI   :  RESEToitu")
    }


    //wanha calibrate()
/*
    private fun calibrate() {

        println("KONSOLI   : calibrate funktio alkaa ...")
        // tähän looppi recordaamaan esim 20 kierrosta ja sitten katsomaan loudest noise.
        //threshold = loudest noise * 0.8

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
*/


    private fun aloita() {

        tv_Wait.visibility = View.VISIBLE

        // jos focus on stopwatchissa niin heittää näppiksen alas ja ottaa myöhemmin focuksen pois
        if (et_Minutes_reaction.isFocused || et_Hours_Reaction.isFocused || et_Seconds_Reaction.isFocused || et_Millis_Reaction.isFocused  ) {
            //tiputtaa näppiksen pois jos vahingossa on jäänyt päälle
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }

        //jottei focus ole valittuna editTexteihin. ettei jää kursori vilkkumaan sinne treenin ajaksi keskelle stopwatchia
        MainLinearLayout.requestFocus()

        //pitää huolen, ettei voi olla tyhjiä aikakenttiä vaan muuttaa ne nolliksi
        if (et_Hours_Reaction.text.toString() == "") {
            et_Hours_Reaction.setText("0",TextView.BufferType.EDITABLE)
        }

        if (et_Minutes_reaction.text.toString() == "") {
            et_Minutes_reaction.setText("0",TextView.BufferType.EDITABLE)
        }

        RandomMin = et_Random_Min.text.toString().toInt() * 1000
        RandomMax = et_Random_Max.text.toString().toInt() * 1000
        RoundStartsIn = et_RoundStartsInSeconds.text.toString().toInt() * 1000

        //poistaa näkyvistä turhat asetukset erän ajaksi
        et_Random_Min.visibility = View.GONE
        et_Random_Max.visibility = View.GONE
        tv_info1.visibility = View.GONE
        tv_info2.visibility = View.GONE
        //tv_info3.visibility = View.GONE
        et_RoundStartsInSeconds.visibility = View.GONE


        //ota NumberPickereistä aika millisekunneiksi
        RoundLengthMillis = (et_Hours_Reaction.text.toString().toLong() * 60 * 60 * 1000) + (et_Minutes_reaction.text.toString().toLong() * 1000 * 60) + (et_Seconds_Reaction.text.toString().toLong() * 1000)
        println("KONSOLI   : ROUNDLENGHT MILLIS : $RoundLengthMillis")

        // RANDOM
        var RandomTimer = Timer()       // tämä on siis vain schedulea varten ei ole itsessään random. Satunnaisgeneraattori on se varsinainen random
        var RandomBeepTime = satunnaisGeneraattori.nextInt(RandomMax-RandomMin) + RandomMin

        //laittaa Round timerin päälle if count == 0
        if (count == 0) {
            runOnUiThread { tv_info3.text = "Wait for BEEP and be fast" }

            //jotta saadaan erän alkuun 5 sec
            var StartUpTimerReaction = Timer()


            //schedule warmup 5 sec
            StartUpTimerReaction.schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        object : CountDownTimer(RoundLengthMillis, 10) {
                            override fun onTick(millisUntilFinished: Long) {

                                aikaTekstiksi = millisUntilFinished
                                millis = (aikaTekstiksi.toInt() % 1000)
                                seconds = (aikaTekstiksi.toInt() / 1000) % 60
                                minutes = (aikaTekstiksi.toInt() / 60000) % 60
                                hours = (aikaTekstiksi.toInt() / 3600000) % 60

                                et_Hours_Reaction.setText(hours.toString(),TextView.BufferType.EDITABLE)
                                et_Minutes_reaction.setText(minutes.toString(),TextView.BufferType.EDITABLE)
                                et_Seconds_Reaction.setText(seconds.toString(),TextView.BufferType.EDITABLE)
                                et_Millis_Reaction.setText(millis.toString(), TextView.BufferType.EDITABLE)
                            }

                            override fun onFinish() {
                                //tv_info.text = "ROUND END"
                                val BoxingBell_ShortLoudEnd = MediaPlayer.create(applicationContext, R.raw.boxingbellshortloud)
                                BoxingBell_ShortLoudEnd.start()
                                RoundHasStarted = false
                                EndTheRound()
                            }

                        }.start()
                    }
                }}, RoundStartsIn.toLong() + RandomBeepTime.toLong())
        }



        // TYÖ pitäskö tähän laittaa mielluummin if CalibrationInProgress == true
        if (count == 0) {
            audioRec!!.stop()
            audioRec!!.release()
            audioRec = null
        }

        // TYÖ  if CalibrationInProgress == true
        CalibrationThread?.interrupt()
        CalibrationInProgress = false


        println("KONSOLI   : randomBeep() $RandomBeepTime")

        //jos eka kierros lähtee pyörimään
        if (count == 0) {
            RandomBeepTime = RandomBeepTime + RoundStartsIn                         // lisää 3 sekunttia EKAAN randomiin
            runOnUiThread { tv_info3.text = "Round starts in ${RoundStartsIn / 1000} + random seconds" }
        }


        //TYÖ nämä on pelkkää testausta varten, eli voi kommentoida pois siksi aikaa kun et käytä näitä
        //var stopWatchSTART = System.currentTimeMillis()
        //var TESTINGScheduleRealTime = 0


        //kierros lähtee tästä
        RandomTimer.schedule(object : TimerTask() {
            override fun run() {                                // run in class TimerTask

                if (RoundEnding == false) {
                    //Random start beeppaus
                    val BeepSound = MediaPlayer.create(applicationContext, R.raw.beep)
                    BeepSound.start()

                    runOnUiThread {
                        tv_info3.text = "GO GO GO !"
                        tv_Wait.visibility = View.GONE
                    }

                    //aloita ajan mittaus ( ReactionHitDetected() saakka )
                    BeepToHitDetectedTIMEstart = System.currentTimeMillis().toInt()

                    //TESTINGScheduleRealTime = System.currentTimeMillis().toInt() - stopWatchSTART.toInt()
                    //println("KONSOLI   : stopWatch millis :  $TESTINGScheduleRealTime")


                    //tämä on varsinainen toiminta. AudioRec ja varsinainen Thread
                    testForHits()

                    println("KONSOLI   : aloita().schedule loppumassa...")
                }

                RandomTimer.cancel()                         // lopettaa schedule loopin
                RandomTimer.purge()
            }

        }, RandomBeepTime.toLong())                                          // jos laittaisit vielä randomSekuLongin perään , x)   -> niin se määrittäisi kaikkien seuraavien viiveen, mutta ne pysyis varmaan samana sit siitä eteenpäin?


    }



    private fun testForHits() {

        // initializing audioRec
        println("KONSOLI   : initializing AudioRec")                            //  TYÖ  jostain syystä tää tulee 2x
        audioRec = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize)

        audioRec!!.startRecording()


        println("KONSOLI   : testForHits.thread alkamassa")
        thread = Thread(Runnable {
            while (thread != null && !thread!!.isInterrupted && RoundEnding == false) {             // && and   || or
                //sleep  for SAMPLE_DELAY tutkimisen välissä
                try {
                    Thread.sleep(SAMPLE_DELAY.toLong())
                } catch (ie: InterruptedException) {
                    ie.printStackTrace()
                }

                MaxAmplitudeTest()      //maxAmp testi

                // tallenna ViimeisinMaxAmplitude taulukkoon ja vertaile for loopin jälkeen k

                if (ViimeisinMaxAmplitude > threshold) { ReactionHitDetected() }

                // jos haluat tallentaa jonnekin isoimman Amplituden
                /*
                if (ViimeisinMaxAmplitude > maxAmpEver) {
                    maxAmpEver = ViimeisinMaxAmplitude.toInt()
                    runOnUiThread { tv_info.text ="max : $maxAmpEver" }
                }
                */



                //TYÖ
                x = x + 1
                series.appendData(DataPoint(x,ViimeisinMaxAmplitude), true, 50)

                Yline = thresholdDouble
                thresholdLineinGraph.appendData(DataPoint(x, Yline), true, 50)


                //tämä tarvii olla jottei kaadu heti 50x mittauksen jälkeen...
                if (x.toInt() % 50 == 1) {
                    graph.removeAllSeries()
                    graph.addSeries(series)
                    graph.addSeries(seriesHITs)
                        graph.addSeries(seriesHITsShadow)
                    graph.addSeries(thresholdLineinGraph)
                }

                println("KONSOLI   :   TOTAL THREADS :  ${Thread.activeCount()} ")

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
                    else                            -> println("KONSOLI   :   total:${Thread.activeCount()}  this.ID:${Thread.currentThread().id}  $SAMPLE_DELAY ms,  ${ViimeisinMaxAmplitude.toInt()} -?????")
                }
            }
        })

        thread!!.start()

    }


    private fun EndTheRound() {
        //tähän kaikki lopetus jutut
        // tallenna score
        // lopeta beeppaukset jne
        // mene kokonaan uuteen activityyn scoren tallennuksen jälkeen?

        val BoxingBell_ShortLoudEnd = MediaPlayer.create(applicationContext, R.raw.boxingbellshortloud)
        BoxingBell_ShortLoudEnd.start()

        RoundEnding = true

        //ei lopeta threadia :(
        thread!!.interrupt()

        SaveScore()

        println("KONSOLI   :  LOPULLINEN KESKIARVO     ----------------------------------------------")
        println("KONSOLI   :  LOPULLINEN KESKIARVO     ${arrayReactionResult!!.average().toInt()} ms")
        println("KONSOLI   :  LOPULLINEN KESKIARVO     ----------------------------------------------")

        //menee Ending Activityyn ja intentillä siirtää tarvittavat datat sinne
        EndingActivityIntent()

    }

    fun EndingActivityIntent() {
        val intentSender = Intent(this, ReactionEndingActivity::class.java)
        val intentCount = count.toString()
        val intentReactionAverage = arrayReactionResult!!.average().toString()
        val intentScoreArray = arrayReactionResult
        intentSender.putExtra(EXTRA_INTENT_COUNT, intentCount)
        intentSender.putExtra(EXTRA_INTENT_REACTION_AVERAGE, intentReactionAverage)
        intentSender.putExtra(EXTRA_INTENT_SCORE_ARRAY, intentScoreArray!!.toIntArray())
        startActivity(intentSender)
    }

    //fun SaveScore (view: View) {
    fun SaveScore () {
        val ScoreToFile : String = "${arrayReactionResult!!.average().toInt().toString()}\n"
        try {
            val fileOutputStream = openFileOutput("score_reaction.txt", Context.MODE_APPEND)    // MODE_APPEND lisää tiedoston perään
            fileOutputStream.write(ScoreToFile.toByteArray())
            fileOutputStream.close()

            println("KONSOLI   : score average saved to file $ScoreToFile")
            runOnUiThread { toast("Your score is saved to YOUR PROGRESS") }

            // throws
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    private fun ReactionHitDetected() {

            //println("KONSOLI   : total:${Thread.activeCount()}  this.ID:${Thread.currentThread().id}  $SAMPLE_DELAY ms,  ${ViimeisinMaxAmplitude.toInt()} BING BING !!!!!")

            runOnUiThread {
                tv_info3.text = "That's a hit, wait for the next BEEP. Your Reaction was $ReactionResultTIME ms"
                tv_Wait.visibility = View.VISIBLE
            }

            count = count + 1
            runOnUiThread { tv_Count.text = "Count : $count" }
            HowLongSinceLastHit = 0

            // näyttää reaktio ajan konsolissa! millisekuntteina
            ReactionResultTIME = System.currentTimeMillis().toInt() - BeepToHitDetectedTIMEstart
            println("KONSOLI   :   varsinainen AIKA :    $ReactionResultTIME")

            //tallenna Result arrayhyn
            arrayReactionResult!!.add(ReactionResultTIME)
            println("KONSOLI   :  ARRAY             : ${arrayReactionResult!!.size}")
            println("KONSOLI   :  ARRAY   keskiarvo : ${arrayReactionResult!!.average().toInt()}")

            //lisää rivi riviltä päälle viimeisimmän ajan.  tarviiko tätä enää
            //runOnUiThread { tvHistory.text = "$ReactionResultTIME \n ${tvHistory.text}" }

            //jottei HIT punainen pallo mene chartista yli...
            if (ViimeisinMaxAmplitude > 29000) {
                seriesHITs.appendData(DataPoint(x, 29000.0), true, 50)
                seriesHITsShadow.appendData(DataPoint(x, 29000.0), true, 50)
            } else {
                seriesHITs.appendData(DataPoint(x, ViimeisinMaxAmplitude), true, 50)
                seriesHITsShadow.appendData(DataPoint(x, ViimeisinMaxAmplitude), true, 50)
            }


            //lisää datan Reaction seriekseen eli alempaan graafiin
            //seriesReaction.appendData(DataPoint(xReaction, ReactionResultTIME.toDouble()), true, 50)

            //tämä tarvii olla jottei kaadu heti 50x mittauksen jälkeen... vai 25x?
            if (xReaction.toInt() % 50 == 1) {
                //historyGraph.removeAllSeries()
                //piirtää käppyrän
                //historyGraph.addSeries(seriesReaction)
            } //else { historyGraph.addSeries(seriesReaction) }

            xReaction = xReaction + 1


            //aloita uudestaan reaktio looppi
            //pitäskö tässä lopettaa AudioRec? ja myös threadit?

                if (audioRec != null) {
                    audioRec!!.stop()
                    audioRec!!.release()
                    audioRec = null
                }

                // mitä tää tekee?!
                thread!!.interrupt()


            aloita()


    }


    // MAX AMPLITUDE tai äänen kovuus testi
/*
    private fun MaxAmplitudeTestBackUp() {

        try {
            val buffer = ShortArray(bufferSize)         // short minimum -32,768 and a maximum 32,767

            var bufferReadResult = 1

            if (audioRec != null) {

                // Sense the voice...
                bufferReadResult = audioRec!!.read(buffer, 0, bufferSize)           // bufferReadResult = 640 eli sama kuin bufferSize
                println("bKONSOLI   :  BufferReadResult : $bufferReadResult / $bufferSize")
                var sumLevel = 0.0

                //TYÖ miksi toDouble? miksei short kävisi?
                //sumLeveliin lisätään jokainen buffer arrayn arvo 0...640 (bufferReadResult)
                for (i in 0..bufferReadResult - 1) {
                    println("cKONSOLI   : $i buffer : ${Math.abs(buffer[i].toInt())}")      //tämä ei näytä ainakaan miinuksia
                    sumLevel += buffer[i].toDouble()    //tämähän plussaa nyt myös miinus äänet!
                }
                ViimeisinMaxAmplitude = Math.abs(sumLevel / bufferReadResult)   // abs = absolute value, eli ei negatiivisia
                println("KONSOLI   : MaxAMP  :  ${ViimeisinMaxAmplitude.toInt()}")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
*/



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

                println("KONSOLI   : BUFFER   $bufferReadResult")

                //TYÖ miksi toDouble? miksei short kävisi?
                //sumLeveliin lisätään jokainen buffer arrayn arvo 0...640 (bufferReadResult)
                for (i in 0..bufferReadResult - 1) {
                    //println("cKONSOLI   : $i buffer : ${Math.abs(buffer[i].toInt())}")      //tämä ei näytä ainakaan miinuksia
                    sumLevel += buffer[i].toDouble()    //tämähän plussaa nyt myös miinus äänet!
                }

                ViimeisinMaxAmplitude = buffer.max()!!.toDouble()
                //println("KONSOLI   : MaxAMP  :  ${ViimeisinMaxAmplitude.toInt()}")
            }

        } catch (e: Exception) {
            println("KONSOLI   : maxAmp val buffer error")
            e.printStackTrace()
        }

    }

/*    // menee on pauseen myös jos laitetta kääntää!
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

    }*/



    override fun onBackPressed() {
        super.onBackPressed()

        thread?.interrupt()
        CalibrationThread!!.interrupt()
        audioRec!!.stop()
        audioRec!!.release()
        audioRec = null
        startActivity(Intent(this@ReactionActivity, MainMenuActivity::class.java))
        finish()

    }

    //vanha setti jolla yritin saada back and reaction again graafin toimimaan
   /*
    override fun onBackPressed() {
        super.onBackPressed()

        println("KONSOLI   : BACK PRESSED")
        println("KONSOLI   : BACK PRESSED")

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

        if (CalibrationThread != null) {
            CalibrationThread!!.interrupt()
            CalibrationThread = null
        }

        series = LineGraphSeries<DataPoint>()
        seriesHITs = PointsGraphSeries<DataPoint>()
        thresholdLineinGraph = LineGraphSeries<DataPoint>()
        seriesReaction = LineGraphSeries<DataPoint>()
        Yline  = 0.0
        x  = 0.0
        xReaction  = 0.0

        seriesHITs = seriesHardReset
        //historyGraph.removeAllSeries()

        graph.removeAllSeries()

        arrayReactionResult?.clear()


    }*/


    //An object declaration inside a class can be marked with the companion keyword
    companion object {

        private var sampleRate = 8000
        private val SAMPLE_DELAY = 40

        //private val LOG_TAG = "AudioRecordTest"
        val REQUEST_RECORD_AUDIO_PERMISSION = 200
        //private var mFileName: String? = null


        //INTENT ja endingActivity
        val EXTRA_INTENT_COUNT =         "com.example.janne.smartstopwatch01.MESSAGE"
        val EXTRA_INTENT_REACTION_AVERAGE  =   "com.example.janne.smartstopwatch01.MESSAGE2"
        val EXTRA_INTENT_SCORE_ARRAY = "com.example.janne.smartstopwatch01.MESSAGE3"
    }


}
