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
}
