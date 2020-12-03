package com.practice.myapplication.model;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    String PREFERENCE = "pref";
    SharedPreferences.Editor editor;

    public boolean getStatus(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getBoolean("status", false);
    }

    public void setStatus(Context context, boolean login) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putBoolean("status", login);
        editor.apply();
    }

    public int getDominantColor(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getInt("color", 0);
    }

    public void setDominantColor(Context context, int color) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putInt("color", color);
        editor.apply();
    }
}
