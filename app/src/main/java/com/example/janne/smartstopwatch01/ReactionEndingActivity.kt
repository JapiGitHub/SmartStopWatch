package com.example.janne.smartstopwatch01

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
        setContentView(R.layout.activity_reaction_ending)

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
        tv_Average_RE.text = "Session average reaction time : ${average}"
        //println("KONSOLI   : $dataa")


        //graph
        var yourProgressReactionRE : GraphView = findViewById(R.id.graph_HistoryRE) as GraphView
        var ThisSessionRE : GraphView = findViewById(R.id.graph_ThisSession) as GraphView


        /*

                    graph.getViewport().setYAxisBoundsManual(true)
            graph.getViewport().setMinY(0.0)
            graphview.getViewport().setMaxY(32000.0)

         */


        seriesReactionScoreHistory = LineGraphSeries<DataPoint>()
        seriesThisSession = LineGraphSeries<DataPoint>()

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






}
