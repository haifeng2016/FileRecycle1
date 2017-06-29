package com.samsung.android.filerecycle.common;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by samsung on 2017/6/23.
 */

public class Util {

    public static final String SP_FIRST_USE = "first_use";

    public static void setSPValue(String key, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RecycleApplication.getApplication());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getSPValue(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RecycleApplication.getApplication());
        return preferences.getString(key, null);
    }

    public static void setSPBooleanValue(String key, boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RecycleApplication.getApplication());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean firstUseCheck() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RecycleApplication.getApplication());
        boolean first = preferences.getBoolean(SP_FIRST_USE, true);
        return first;
    }
}
