package com.example.janne.smartstopwatch01

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_count_history.*
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException

class CountHistoryActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_count_history)

        supportActionBar!!.hide()


        read()
    }


    fun read () {
        try {                           //   HUOM  HUOM!!! VÄÄRÄ TIEDOSTONIMI
            val fileInputStream = openFileInput("score_reaction.txt")

            // tämä on sen JAVAn vastaava   :     while ( (line = reader.readLine()) != null )   { result += line; }
            val allText = fileInputStream.bufferedReader().use(BufferedReader::readText)

            tv_counterHistoryInfo.text = allText

            //throws
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

}
