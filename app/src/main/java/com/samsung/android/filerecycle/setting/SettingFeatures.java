package com.samsung.android.filerecycle.setting;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StringDef;

import com.samsung.android.filerecycle.common.RecycleApplication;

/**
 * Created by samsung on 2017/6/23.
 */

public class SettingFeatures {

    public static final String KEY_SWITCH_PIC = "switch_pic";
    public static final String KEY_SWITCH_VIDEO = "switch_video";
    public static final String KEY_SWITCH_MUSIC = "switch_music";
    public static final String KEY_SWITCH_DOC = "switch_doc";
    public static final String KEY_SWITCH_APP = "switch_app";
    public static final String KEY_SWITCH_OTHER = "switch_other";
    public static final String KEY_KEEP_DURATION = "storage_time";

    @StringDef({
            KEY_SWITCH_PIC, KEY_SWITCH_VIDEO, KEY_SWITCH_MUSIC,
            KEY_SWITCH_DOC, KEY_SWITCH_APP, KEY_SWITCH_OTHER, KEY_KEEP_DURATION
    })
    @interface SupportFeature {
    }

    public static void setSwitchState(@SupportFeature String key, boolean value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RecycleApplication.getApplication());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getSwitchState(@SupportFeature String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RecycleApplication.getApplication());
        return preferences.getBoolean(key, true);
    }

    public static boolean getSaveTimeSwitchState(@SupportFeature String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RecycleApplication.getApplication());
        return preferences.getBoolean(key, false);
    }

}
