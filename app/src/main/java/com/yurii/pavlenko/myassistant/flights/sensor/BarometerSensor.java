package com.yurii.pavlenko.myassistant.flights.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/** The phone's air pressure sensor, which feels a change of height within a second. */
public final class BarometerSensor implements SensorEventListener {

    private static final long NANOS_PER_MILLI = 1_000_000;

    /** Receives a pressure in hectopascals with the time of the reading in milliseconds. */
    public interface Listener {
        void onPressure(long timeMillis, double hectopascals);
    }

    private final SensorManager manager;
    private final Sensor pressure;
    private final Listener listener;

    public BarometerSensor(Context context, Listener listener) {
        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        pressure = manager == null ? null : manager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        this.listener = listener;
    }

    public boolean isAvailable() {
        return pressure != null;
    }

    public void start() {
        if (pressure != null) {
            manager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void stop() {
        if (manager != null) {
            manager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        listener.onPressure(event.timestamp / NANOS_PER_MILLI, event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
