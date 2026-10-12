package com.yurii.pavlenko.myassistant.steps.data;

import android.content.Context;

import com.yurii.pavlenko.myassistant.steps.database.StepDao;
import com.yurii.pavlenko.myassistant.steps.database.StepDatabase;
import com.yurii.pavlenko.myassistant.steps.logic.CounterTracker;
import com.yurii.pavlenko.myassistant.steps.logic.DaySplitter;
import com.yurii.pavlenko.myassistant.steps.logic.DayTotals;
import com.yurii.pavlenko.myassistant.steps.logic.StepBurst;
import com.yurii.pavlenko.myassistant.steps.logic.StepLengths;
import com.yurii.pavlenko.myassistant.steps.logic.StepSample;
import com.yurii.pavlenko.myassistant.steps.model.StepSettings;
import com.yurii.pavlenko.myassistant.steps.sensor.StepCounterReader;

import java.time.ZoneId;

/** Turns counter readings into stored daily steps. Blocking: call it off the main thread. */
public final class StepRecorder {

    private static final Object LOCK = new Object();

    private final Context context;

    public StepRecorder(Context context) {
        this.context = context.getApplicationContext();
    }

    /** Reads the counter now and records the steps taken since the previous reading. */
    public void recordNow() {
        long counter = StepCounterReader.readBlocking(context);
        if (counter != StepCounterReader.UNAVAILABLE) {
            record(counter, System.currentTimeMillis());
        }
    }

    public void record(long counter, long nowMillis) {
        synchronized (LOCK) {
            StepBaselineStore baselineStore = new StepBaselineStore(context);
            StepSample current = new StepSample(counter, nowMillis);
            StepBurst burst = CounterTracker.between(baselineStore.load(), current);
            if (burst != null) {
                store(burst);
            }
            baselineStore.save(current);
        }
    }

    private void store(StepBurst burst) {
        StepDao dao = StepDatabase.getInstance(context).stepDao();
        StepSettings saved = dao.settings();
        StepLengths lengths = saved == null ? StepLengths.defaults() : saved.toLengths();
        for (DayTotals share : DaySplitter.split(burst, lengths.getRunCadence(), ZoneId.systemDefault())) {
            dao.addSteps(share.getEpochDay(), share.getWalkSteps(), share.getRunSteps());
        }
    }
}
