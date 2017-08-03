package com.example.janne.smartstopwatch01

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import org.jetbrains.anko.button
import org.jetbrains.anko.editText
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast
import org.jetbrains.anko.verticalLayout
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenuActivity : AppCompatActivity() {

    //JAVA napit. init() tehtiin onCreaten lopussa
    /*
    //napit main menuun
    var buttReaction: Button
    var buttCountREPs: Button? = null
    var buttSettings: Button? = null
    var buttReactionHistory: Button? = null
    var buttCountHistory: Button? = null
*/
    /*
    fun init() {

        //nappi reaction activityyn
        buttReaction = findViewById(R.id.buttReaction) as Button
        buttReaction.setOnClickListener {
            val toy = Intent(this@MainMenuActivity, ReactionActivity::class.java)

            startActivity(toy)
        }


        //nappi Counter activityyn
        buttReaction = findViewById(R.id.buttCountREPs) as Button
        buttReaction.setOnClickListener {
            val toyy = Intent(this@MainMenuActivity, CounterActivity::class.java)

            startActivity(toyy)
        }


        //nappi settings activityyn
        buttReaction = findViewById(R.id.buttSettings) as Button
        buttReaction.setOnClickListener {
            val toyyy = Intent(this@MainMenuActivity, SettingsActivity::class.java)

            startActivity(toyyy)
        }


        //nappi settings activityyn
        buttReaction = findViewById(R.id.buttReactionHistory) as Button
        buttReaction.setOnClickListener {
            val toyyyy = Intent(this@MainMenuActivity, ReactionHistoryActivity::class.java)

            startActivity(toyyyy)
        }


        //nappi settings activityyn
        buttReaction = findViewById(R.id.buttCountHistory) as Button
        buttReaction.setOnClickListener {
            val toyyys = Intent(this@MainMenuActivity, CountHistoryActivity::class.java)

            startActivity(toyyys)
        }


    }
*/

    val REQUEST_RECORD_AUDIO_PERMISSION = 200


    // Permissions jatkuu onCreatesssa (activityCompat.request...)   ja   lopussa companion object (private val REQUEST_RECORD_AUDIO_PERMISSION = 200)
    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    //tähän onRequestPermissionResult
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ReactionActivity.REQUEST_RECORD_AUDIO_PERMISSION -> permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionToRecordAccepted) finish()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)






        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main_menu)
        supportActionBar!!.hide()

        //permissions
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)

        buttReaction.onClick        { startActivity(Intent(this@MainMenuActivity,ReactionActivity::class.java)) }
        buttReactionHistory.onClick { startActivity(Intent(this@MainMenuActivity,ReactionHistoryActivity::class.java)) }
        buttCountREPs.onClick       { startActivity(Intent(this@MainMenuActivity,CounterActivity::class.java)) }
        buttCountHistory.onClick    { startActivity(Intent(this@MainMenuActivity,CountHistoryActivity::class.java)) }
        buttSettings.onClick        { startActivity(Intent(this@MainMenuActivity,SettingsActivity::class.java)) }



    }




}