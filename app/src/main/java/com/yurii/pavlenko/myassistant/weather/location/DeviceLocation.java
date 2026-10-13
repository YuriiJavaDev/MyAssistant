package com.yurii.pavlenko.myassistant.weather.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.os.CancellationSignal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Finds where the phone is, with city-level accuracy. */
public final class DeviceLocation {

    private static final long FRESH_MILLIS = TimeUnit.MINUTES.toMillis(30);
    private static final long TIMEOUT_SECONDS = 10;

    private DeviceLocation() {
    }

    public static boolean hasPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Returns null when no position is available. Blocks, so it must not be called on the main thread. */
    public static Location find(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null || !hasPermission(context)) {
            return null;
        }
        Location known = newestKnown(manager);
        if (known != null && System.currentTimeMillis() - known.getTime() < FRESH_MILLIS) {
            return known;
        }
        Location current = requestCurrent(context, manager);
        return current != null ? current : known;
    }

    private static Location newestKnown(LocationManager manager) {
        Location newest = null;
        for (String provider : manager.getProviders(true)) {
            Location location = manager.getLastKnownLocation(provider);
            if (location != null && (newest == null || location.getTime() > newest.getTime())) {
                newest = location;
            }
        }
        return newest;
    }

    private static Location requestCurrent(Context context, LocationManager manager) {
        String provider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                ? LocationManager.NETWORK_PROVIDER
                : LocationManager.GPS_PROVIDER;
        if (!manager.isProviderEnabled(provider)) {
            return null;
        }
        AtomicReference<Location> result = new AtomicReference<>();
        CountDownLatch received = new CountDownLatch(1);
        CancellationSignal cancellation = new CancellationSignal();
        LocationManagerCompat.getCurrentLocation(manager, provider, cancellation,
                ContextCompat.getMainExecutor(context), location -> {
                    result.set(location);
                    received.countDown();
                });
        try {
            received.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            cancellation.cancel();
        }
        return result.get();
    }
}
