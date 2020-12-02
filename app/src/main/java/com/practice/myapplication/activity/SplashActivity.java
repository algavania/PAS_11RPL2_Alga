package com.practice.myapplication.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.practice.myapplication.R;
import com.practice.myapplication.model.ControlClass;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();

        /****** Create Thread that will sleep for 3 seconds****/
        Thread background = new Thread() {
            public void run() {
                try {
                    // Thread will sleep for 3 seconds
                    sleep(3*1000);
                    Intent i=new Intent(getBaseContext(), ControlClass.class);
                    startActivity(i);
                    finish();
                } catch (Exception e) {
                }
            }
        };
        // start thread
        background.start();
    }
}