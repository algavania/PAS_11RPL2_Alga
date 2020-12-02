package com.practice.myapplication.model;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.practice.myapplication.activity.HomeActivity;
import com.practice.myapplication.activity.LoginActivity;

public class ControlClass extends AppCompatActivity {

    Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = new Preferences();

        if (preferences.getStatus(getApplicationContext())) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            Log.d("Dashboard", "yes");
        } else {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            Log.d("Login", "yes");
        }
        finish();
    }
}