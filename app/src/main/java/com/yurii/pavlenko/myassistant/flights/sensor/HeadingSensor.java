package com.yurii.pavlenko.myassistant.flights.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.yurii.pavlenko.myassistant.flights.logic.HeadingMath;

import java.util.function.DoubleConsumer;

/** The phone's compass: reports the magnetic heading in degrees. */
public final class HeadingSensor implements SensorEventListener {

    private static final int MATRIX_SIZE = 9;

    private final SensorManager manager;
    private final Sensor rotation;
    private final DoubleConsumer onHeading;
    private final float[] matrix = new float[MATRIX_SIZE];

    public HeadingSensor(Context context, DoubleConsumer onHeading) {
        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotation = manager == null ? null : manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        this.onHeading = onHeading;
    }

    public boolean isAvailable() {
        return rotation != null;
    }

    public void start() {
        if (rotation != null) {
            manager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        if (manager != null) {
            manager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorManager.getRotationMatrixFromVector(matrix, event.values);
        onHeading.accept(HeadingMath.fromRotationMatrix(matrix));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
