package com.example.janne.smartstopwatch01

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Chronometer
import android.os.SystemClock
import kotlinx.android.synthetic.main.activity_chrono_test.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.text.DateFormat

class chronoTest : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chrono_test)

        btn_Start.onClick        { StartChrono() }
        textView8.text = ChronoView.format
       // textView8.setText(DateFormat("H:mm:ss", calendar.Time))
    }



    fun StartChrono() {
        //ChronoView.base(SystemClock.elapsedRealtime())
        ChronoView.start()
    }

}
