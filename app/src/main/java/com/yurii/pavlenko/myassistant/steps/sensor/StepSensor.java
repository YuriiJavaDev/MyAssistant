package com.yurii.pavlenko.myassistant.steps.sensor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

public final class StepSensor {

    private StepSensor() {
    }

    public static Sensor find(Context context) {
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        return manager == null ? null : manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }

    public static boolean isAvailable(Context context) {
        return find(context) != null;
    }

    public static boolean hasPermission(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED;
    }
}
