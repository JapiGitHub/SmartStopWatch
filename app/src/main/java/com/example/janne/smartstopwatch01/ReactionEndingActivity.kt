package com.example.janne.smartstopwatch01

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_reaction_ending.*
import java.io.File
import java.io.InputStream

class ReactionEndingActivity : AppCompatActivity() {

    lateinit var seriesReactionScoreHistory: LineGraphSeries<DataPoint>
    lateinit var seriesThisSession : LineGraphSeries<DataPoint>
    var x : Double = 0.0
    var HistoryMax : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_reaction_ending)
        supportActionBar!!.hide()

        // pitää portraittina!
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        //INTENT DATAn HAKU
        val intent = getIntent()
        val EndCount = intent.getStringExtra(ReactionActivity.EXTRA_INTENT_COUNT)
        val average = intent.getStringExtra(ReactionActivity.EXTRA_INTENT_REACTION_AVERAGE)
        val ScoreArray = intent.getIntArrayExtra(ReactionActivity.EXTRA_INTENT_SCORE_ARRAY)
        // Capture the layout's TextView and set the string as its text
        //val textView = findViewById(R.id.textView) as TextView
        println("KONSOLI   : End Count  : $EndCount")
        //println("KONSOLI   : ARRAYYYY ${ScoreArray[1]}")
        tv_Count_RE.text = "Total count : $EndCount"
        tv_Average_RE.text = "Session average reaction time : ${(average.toDouble().toInt().toDouble())/1000}  seconds"
        //println("KONSOLI   : $dataa")


        //graph
        var yourProgressReactionRE = findViewById<GraphView>(R.id.graph_HistoryRE)
        var ThisSessionRE  = findViewById<GraphView>(R.id.graph_ThisSession)

        /*

                    graph.getViewport().setYAxisBoundsManual(true)
            graph.getViewport().setMinY(0.0)
            graphview.getViewport().setMaxY(32000.0)


            series.color = Color.TRANSPARENT
            series.isDrawBackground = true
            val graphColorBluish = Color.argb(200, 161, 161, 147)
            series.backgroundColor = graphColorBluish


            //threshold line
            val ThresholdColorBlue = Color.argb(200,255,102,0)
            thresholdLineinGraph.color = ThresholdColorBlue
            thresholdLineinGraph.thickness = 7

            graph.gridLabelRenderer.isVerticalLabelsVisible = false
            graph.gridLabelRenderer.isHorizontalLabelsVisible = false
            graph.gridLabelRenderer.isHighlightZeroLines = false

            graph.gridLabelRenderer.gridColor = 0


         */


        
        seriesThisSession = LineGraphSeries<DataPoint>()

        seriesThisSession.isDrawBackground = true
        val graphColorBluish = Color.argb(200, 161, 161, 147)
        seriesThisSession.backgroundColor = graphColorBluish
        seriesThisSession.color = Color.TRANSPARENT
        ThisSessionRE.gridLabelRenderer.isVerticalLabelsVisible = true
        ThisSessionRE.gridLabelRenderer.isHorizontalLabelsVisible = false
        ThisSessionRE.gridLabelRenderer.isHighlightZeroLines = false
        ThisSessionRE.gridLabelRenderer.gridColor = 0


        seriesReactionScoreHistory = LineGraphSeries<DataPoint>()
        seriesReactionScoreHistory.color = Color.TRANSPARENT
        seriesReactionScoreHistory.isDrawBackground = true
        seriesReactionScoreHistory.backgroundColor = graphColorBluish
        yourProgressReactionRE.gridLabelRenderer.isVerticalLabelsVisible = true
        yourProgressReactionRE.gridLabelRenderer.isHorizontalLabelsVisible = false
        yourProgressReactionRE.gridLabelRenderer.isHighlightZeroLines = false
        yourProgressReactionRE.gridLabelRenderer.gridColor = 0
        
        
        
        //this session
        for (i in 0..(ScoreArray.size-1)) {
            println("KONSOLI   : score this session :  ${ScoreArray[i]}")
            seriesThisSession.appendData(DataPoint((i+1.toDouble()),ScoreArray[i].toDouble()), true, 25)
        }


        // lataa tiedostosta historiaa
        val path = getFilesDir()
        println("KONSOLI   : path :  $path")

        //val inputStream : InputStream = File("/data/user/0/com.example.janne.smartstopwatch01/files/score_reaction.txt").inputStream()
        val inputStream : InputStream = File("$path/score_reaction.txt").inputStream()
        val lineList = mutableListOf<String>()

        inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it)} }

        //history
        //lineList.forEach{println(">  " + it)}
        for (i in 0..(lineList.size-1)) {
            println("KONSOLI   : lineLIST :  ${lineList[i]}")
            seriesReactionScoreHistory.appendData(DataPoint((i+1.toDouble()),lineList[i].toDouble()), true, 25)
        }

        var LineListINTEGERmutab : MutableList<Int> = mutableListOf(1)

        for (i in 0..(lineList.size-1)) {
            LineListINTEGERmutab.add(lineList[i].toInt())
        }

        HistoryMax = LineListINTEGERmutab.max()!!.toDouble()

        yourProgressReactionRE.getViewport().setYAxisBoundsManual(true)
        yourProgressReactionRE.getViewport().setMaxY(HistoryMax)



        //graph
        //seriesReactionScore.appendData(DataPoint(1.0,250.0), true, 25)

        graph_HistoryRE.addSeries(seriesReactionScoreHistory)
        graph_ThisSession.addSeries(seriesThisSession)



    }


    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@ReactionEndingActivity, MainMenuActivity::class.java))
        finish()

    }



}
