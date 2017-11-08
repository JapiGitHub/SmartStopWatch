package com.example.janne.smartstopwatch01

import android.content.Context
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

import kotlinx.android.synthetic.main.activity_reaction_history.*
import java.io.*

class ReactionHistoryActivity : AppCompatActivity() {

    //graph
    lateinit var seriesReactionScore: LineGraphSeries<DataPoint>
    var x : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_reaction_history)

        supportActionBar!!.hide()

        //graph
        var yourProgressReaction = findViewById<GraphView>(R.id.yourProgressReaction)
        seriesReactionScore = LineGraphSeries<DataPoint>()


        seriesReactionScore.color = Color.TRANSPARENT
        seriesReactionScore.isDrawBackground = true
        val graphColorBluish = Color.argb(200, 161, 161, 147)
        seriesReactionScore.backgroundColor = graphColorBluish


        yourProgressReaction.gridLabelRenderer.isVerticalLabelsVisible = true
        yourProgressReaction.gridLabelRenderer.isHorizontalLabelsVisible = false
        yourProgressReaction.gridLabelRenderer.isHighlightZeroLines = false

        yourProgressReaction.gridLabelRenderer.gridColor = 0

        yourProgressReaction.getViewport().setScrollable(true)

        //read()

        readLines()

    }


/*    private fun read () {
        try {
            var fileInputStream = openFileInput("score_reaction.txt")

            var allText = fileInputStream.bufferedReader().use(BufferedReader::readText)        // tämä on sen JAVAn vastaava   :     while ( (line = reader.readLine()) != null )   { result += line; }

            println("KONSOLI   : allText:  $allText")

            //runOnUiThread { tv_ReactionInfo.text = allText.toString() }

            //throws
        } catch (e: FileNotFoundException) {
            println("KONSOLI   : file not found")
            e.printStackTrace()
        } catch (e: IOException) {
            println("KONSOLI   : IO exception")
            e.printStackTrace()
        }

    }*/


    private fun readLines () {

        val path = getFilesDir()
        println("KONSOLI   : path :  $path")

        //val inputStream : InputStream = File("/data/user/0/com.example.janne.smartstopwatch01/files/score_reaction.txt").inputStream()
        val inputStream : InputStream = File("$path/score_reaction.txt").inputStream()
        val lineList = mutableListOf<String>()

        inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it)} }

        //lineList.forEach{println(">  " + it)}
        for (i in 0..(lineList.size-1)) {
            println("KONSOLI   : lineLIST :  ${lineList[i]}")
            seriesReactionScore.appendData(DataPoint((i+1.toDouble()),lineList[i].toDouble()), true, 25)
        }


        //graph
        //seriesReactionScore.appendData(DataPoint(1.0,250.0), true, 25)

        yourProgressReaction.addSeries(seriesReactionScore)


    }

}
