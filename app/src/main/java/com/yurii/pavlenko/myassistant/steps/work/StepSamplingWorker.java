package com.yurii.pavlenko.myassistant.steps.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.yurii.pavlenko.myassistant.steps.data.StepRecorder;

/** Periodically records the steps taken since the last reading, so counting continues while the app is closed. */
public class StepSamplingWorker extends Worker {

    public StepSamplingWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        new StepRecorder(getApplicationContext()).recordNow();
        return Result.success();
    }
}
