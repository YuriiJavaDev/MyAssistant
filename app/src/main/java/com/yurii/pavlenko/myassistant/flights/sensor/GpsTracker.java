package com.yurii.pavlenko.myassistant.flights.sensor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.function.Consumer;

/** Receives a GPS position about once a second. It works without internet, also in flight mode. */
public final class GpsTracker implements LocationListener {

    private static final long INTERVAL_MILLIS = 1000;

    private final LocationManager manager;
    private final Context context;
    private final Consumer<Location> onFix;

    public GpsTracker(Context context, Consumer<Location> onFix) {
        this.context = context.getApplicationContext();
        manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.onFix = onFix;
    }

    public static boolean hasPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isSwitchedOn(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager != null && manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /** Returns false when GPS cannot be used: no permission, or GPS is switched off. */
    public boolean start() {
        if (manager == null || !hasPermission(context) || !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }
        try {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL_MILLIS, 0f, this, Looper.getMainLooper());
            return true;
        } catch (SecurityException permissionRevoked) {
            return false;
        }
    }

    public void stop() {
        if (manager != null) {
            manager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        onFix.accept(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }
}
