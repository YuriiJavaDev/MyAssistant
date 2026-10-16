package com.yurii.pavlenko.myassistant.flights.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.view.Display;

import com.yurii.pavlenko.myassistant.flights.logic.Orientation;

import java.util.function.Consumer;

/** The phone's compass and level: reports the magnetic heading, the pitch and the roll, as seen from the screen in whichever way it is turned. */
public final class OrientationSensor implements SensorEventListener {

    private static final int MATRIX_SIZE = 9;

    private final SensorManager manager;
    private final DisplayManager displays;
    private final Sensor rotation;
    private final Consumer<Orientation> onOrientation;
    private final float[] matrix = new float[MATRIX_SIZE];

    public OrientationSensor(Context context, Consumer<Orientation> onOrientation) {
        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        displays = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        rotation = manager == null ? null : manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        this.onOrientation = onOrientation;
    }

    public boolean isAvailable() {
        return rotation != null;
    }

    public void start() {
        if (rotation != null) {
            manager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_GAME);
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
        onOrientation.accept(Orientation.fromRotationMatrix(matrix, displayRotation()));
    }

    /** How far the screen is turned from natural, in quarter turns; the phone is read as the user holds it. */
    private int displayRotation() {
        Display display = displays == null ? null : displays.getDisplay(Display.DEFAULT_DISPLAY);
        return display == null ? 0 : display.getRotation();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
