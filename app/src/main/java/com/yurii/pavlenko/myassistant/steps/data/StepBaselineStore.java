package com.yurii.pavlenko.myassistant.steps.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.yurii.pavlenko.myassistant.steps.logic.StepSample;

/** The last counter reading. It describes this phone's counter, so it is kept out of the backed-up database. */
final class StepBaselineStore {

    private static final String PREFS = "step_baseline";
    private static final String KEY_COUNTER = "counter";
    private static final String KEY_TIME = "time";

    private final SharedPreferences prefs;

    StepBaselineStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    StepSample load() {
        if (!prefs.contains(KEY_COUNTER)) {
            return null;
        }
        return new StepSample(prefs.getLong(KEY_COUNTER, 0), prefs.getLong(KEY_TIME, 0));
    }

    void save(StepSample sample) {
        prefs.edit().putLong(KEY_COUNTER, sample.getCounter()).putLong(KEY_TIME, sample.getTimeMillis()).apply();
    }
}
