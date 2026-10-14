package com.yurii.pavlenko.myassistant.flights.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.function.DoubleConsumer;

/** Measures the load on the phone in units of gravity, which is 1 when it lies still. */
public final class GForceSensor implements SensorEventListener {

    private final SensorManager manager;
    private final Sensor accelerometer;
    private final DoubleConsumer onGForce;

    public GForceSensor(Context context, DoubleConsumer onGForce) {
        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = manager == null ? null : manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.onGForce = onGForce;
    }

    public void start() {
        if (accelerometer != null) {
            manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        if (manager != null) {
            manager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];
        onGForce.accept(Math.sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
