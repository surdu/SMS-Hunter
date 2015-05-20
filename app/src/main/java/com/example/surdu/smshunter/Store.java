package com.example.surdu.smshunter;

import android.content.Context;
import android.content.SharedPreferences;

public class Store {

    public static Context context = null;

    public static void set(String prefName, String prefValue) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(prefName, prefValue);
        edit.commit();

    }

    public static String get(String prefName) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", 0);
        return  prefs.getString(prefName, null);
    }

}
