package com.example.janne.smartstopwatch01;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.*;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;


public class MainMenuActivity extends AppCompatActivity {


    //napit main menuun
    public Button buttReaction;
    public Button buttCountREPs;
    public Button buttSettings;
    public Button buttReactionHistory;
    public Button buttCountHistory;




    public void init() {

        //nappi reaction activityyn
        buttReaction = (Button)findViewById(R.id.buttReaction);
        buttReaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toy = new Intent(MainMenuActivity.this, ReactionActivity.class);

                startActivity(toy);
            }
        });


        //nappi Counter activityyn
        buttReaction = (Button)findViewById(R.id.buttCountREPs);
        buttReaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toyy = new Intent(MainMenuActivity.this, CounterActivity.class);

                startActivity(toyy);
            }
        });


        //nappi settings activityyn
        buttReaction = (Button)findViewById(R.id.buttSettings);
        buttReaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toyyy = new Intent(MainMenuActivity.this, SettingsActivity.class);

                startActivity(toyyy);
            }
        });


        //nappi settings activityyn
        buttReaction = (Button)findViewById(R.id.buttReactionHistory);
        buttReaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toyyyy = new Intent(MainMenuActivity.this, ReactionHistoryActivity.class);

                startActivity(toyyyy);
            }
        });



        //nappi settings activityyn
        buttReaction = (Button)findViewById(R.id.buttCountHistory);
        buttReaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toyyys = new Intent(MainMenuActivity.this, CountHistoryActivity.class);

                startActivity(toyyys);
            }
        });




    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main_menu);
        getSupportActionBar().hide();

        init();
    }
}