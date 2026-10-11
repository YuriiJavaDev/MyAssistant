package com.yurii.pavlenko.myassistant.ui.theme;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/** Remembers the chosen Day / Night / System mode, which AppCompat itself forgets when the app restarts. */
public final class DisplayModeStore {

    private static final String PREFS_NAME = "myassistant_display";
    private static final String KEY_NIGHT_MODE = "night_mode";

    private DisplayModeStore() {
    }

    /** Applies the saved mode; call once when the app process starts. */
    public static void apply(Context context) {
        AppCompatDelegate.setDefaultNightMode(preferences(context).getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));
    }

    /** Saves the mode and applies it at once. */
    public static void set(Context context, int nightMode) {
        preferences(context).edit().putInt(KEY_NIGHT_MODE, nightMode).apply();
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
