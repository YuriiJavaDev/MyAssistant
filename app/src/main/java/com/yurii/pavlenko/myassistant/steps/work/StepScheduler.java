package com.yurii.pavlenko.myassistant.steps.work;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.yurii.pavlenko.myassistant.steps.sensor.StepSensor;

import java.util.concurrent.TimeUnit;

public final class StepScheduler {

    private static final String WORK_NAME = "step_sampling";
    private static final long INTERVAL_MINUTES = 15;

    private StepScheduler() {
    }

    public static void schedule(Context context) {
        if (!StepSensor.isAvailable(context)) {
            return;
        }
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                StepSamplingWorker.class, INTERVAL_MINUTES, TimeUnit.MINUTES).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }
}
