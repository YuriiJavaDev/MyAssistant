package com.yurii.pavlenko.myassistant.flights.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.yurii.pavlenko.myassistant.flights.logic.VarioSoundSettings;

/** Remembers the sound settings of the variometer. */
public final class VarioSoundStore {

    private static final String PREFS = "vario_sound";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_CLIMB_START = "climb_start";
    private static final String KEY_SINK_START = "sink_start";
    private static final String KEY_SINK_ALARM = "sink_alarm";

    private final SharedPreferences prefs;

    public VarioSoundStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public VarioSoundSettings load() {
        VarioSoundSettings defaults = VarioSoundSettings.defaults();
        return new VarioSoundSettings(
                prefs.getBoolean(KEY_ENABLED, defaults.isEnabled()),
                prefs.getFloat(KEY_CLIMB_START, (float) defaults.getClimbStart()),
                prefs.getFloat(KEY_SINK_START, (float) defaults.getSinkStart()),
                prefs.getFloat(KEY_SINK_ALARM, (float) defaults.getSinkAlarm()));
    }

    public void save(VarioSoundSettings settings) {
        prefs.edit()
                .putBoolean(KEY_ENABLED, settings.isEnabled())
                .putFloat(KEY_CLIMB_START, (float) settings.getClimbStart())
                .putFloat(KEY_SINK_START, (float) settings.getSinkStart())
                .putFloat(KEY_SINK_ALARM, (float) settings.getSinkAlarm())
                .apply();
    }
}
