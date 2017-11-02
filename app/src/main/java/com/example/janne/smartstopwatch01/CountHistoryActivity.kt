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
import kotlinx.android.synthetic.main.activity_count_history.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast
import java.io.*

class CountHistoryActivity : AppCompatActivity() {

    lateinit var seriesCountScore: LineGraphSeries<DataPoint>
    var x: Double = 0.0
    private var cumulativeCount : Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_count_history)

        supportActionBar!!.hide()

        var yourProgressCount: GraphView = findViewById(R.id.CountHistoryGraph) as GraphView
        seriesCountScore = LineGraphSeries<DataPoint>()



        seriesCountScore.color = Color.TRANSPARENT
        seriesCountScore.isDrawBackground = true
        val graphColorBluish = Color.argb(200, 161, 161, 147)
        seriesCountScore.backgroundColor = graphColorBluish


        CountHistoryGraph.gridLabelRenderer.isVerticalLabelsVisible = true
        CountHistoryGraph.gridLabelRenderer.isHorizontalLabelsVisible = false
        CountHistoryGraph.gridLabelRenderer.isHighlightZeroLines = false

        CountHistoryGraph.gridLabelRenderer.gridColor = 0

        yourProgressCount.getViewport().setScrollable(true)

        btn_ResetCountHistory.onClick { resetCountHistory() }


        readLines()
    }

    private fun resetCountHistory() {
        val ScoreToFile : String = "0"
        try {
            val fileOutputStream = openFileOutput("score_counter.txt", Context.MODE_PRIVATE)    // MODE_APPEND lisää tiedoston perään
            fileOutputStream.write(ScoreToFile.toByteArray())
            fileOutputStream.close()

            runOnUiThread { toast("Your score was reseted") }

            // throws
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


/*    fun read() {
        try {                           //   HUOM  HUOM!!! VÄÄRÄ TIEDOSTONIMI
            val fileInputStream = openFileInput("score_counter.txt")

            // tämä on sen JAVAn vastaava   :     while ( (line = reader.readLine()) != null )   { result += line; }
            val allText = fileInputStream.bufferedReader().use(BufferedReader::readText)

            textView6.text = allText

            //throws
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        CountHistoryGraph.addSeries(seriesCountScore)

    }*/


    fun readLines() {

        val path = getFilesDir()
        println("KONSOLI   : path :  $path")

        //val inputStream : InputStream = File("/data/user/0/com.example.janne.smartstopwatch01/files/score_reaction.txt").inputStream()
        val inputStream: InputStream = File("$path/score_counter.txt").inputStream()
        val lineList = mutableListOf<String>()

        inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it) } }

        //lineList.forEach{println(">  " + it)}
        for (i in 0..(lineList.size - 1)) {
            println("KONSOLI   : lineLIST :  ${lineList[i]}")
            cumulativeCount = cumulativeCount + lineList[i].toLong()
            seriesCountScore.appendData(DataPoint((i + 1.toDouble()), cumulativeCount.toDouble()), true, 25)
        }

        CountHistoryGraph.addSeries(seriesCountScore)

    }


}