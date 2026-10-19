package com.yurii.pavlenko.myassistant.ui.theme;

import android.content.Context;
import android.content.SharedPreferences;

/** Remembers the chosen design between launches. */
public final class DesignStore {

    private static final String PREFS_NAME = "myassistant_design";
    private static final String KEY_DESIGN = "design";

    private DesignStore() {
    }

    public static AppDesign get(Context context) {
        return AppDesign.fromId(preferences(context).getString(KEY_DESIGN, null));
    }

    public static void set(Context context, AppDesign design) {
        preferences(context).edit().putString(KEY_DESIGN, design.id).apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
