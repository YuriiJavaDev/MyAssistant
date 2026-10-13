package com.yurii.pavlenko.myassistant.steps.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class StepCounterReader {

    public static final long UNAVAILABLE = -1;

    private static final long TIMEOUT_SECONDS = 5;

    private StepCounterReader() {
    }

    /** Blocks until the sensor reports once; must not be called on the main thread. */
    public static long readBlocking(Context context) {
        Sensor sensor = StepSensor.find(context);
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensor == null || manager == null || !StepSensor.hasPermission(context)) {
            return UNAVAILABLE;
        }
        AtomicLong value = new AtomicLong(UNAVAILABLE);
        CountDownLatch received = new CountDownLatch(1);
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                value.set((long) event.values[0]);
                received.countDown();
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        if (!manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)) {
            return UNAVAILABLE;
        }
        try {
            received.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            manager.unregisterListener(listener);
        }
        return value.get();
    }
}
