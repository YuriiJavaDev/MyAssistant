package com.yurii.pavlenko.myassistant.tasks.ui.handlers;

import android.content.Context;
import android.content.SharedPreferences;

public class TaskPreferences {

    private static final String PREF_NAME = "task_reminder_prefs";
    private static final String KEY_VOICE_ENABLED = "key_voice_enabled";
    private static final String KEY_FULL_SCREEN_ENABLED = "key_full_screen_enabled";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isVoiceEnabled(Context context) {
        // Enabled by default
        return getPrefs(context).getBoolean(KEY_VOICE_ENABLED, true);
    }

    public static void setVoiceEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_VOICE_ENABLED, enabled).apply();
    }

    public static boolean isFullScreenEnabled(Context context) {
        // Enabled by default
        return getPrefs(context).getBoolean(KEY_FULL_SCREEN_ENABLED, true);
    }

    public static void setFullScreenEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_FULL_SCREEN_ENABLED, enabled).apply();
    }
}