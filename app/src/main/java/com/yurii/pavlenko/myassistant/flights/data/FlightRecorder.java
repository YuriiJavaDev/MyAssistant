package com.yurii.pavlenko.myassistant.flights.data;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.os.Build;
import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yurii.pavlenko.myassistant.flights.logic.AngleSmoother;
import com.yurii.pavlenko.myassistant.flights.logic.FlightPoint;
import com.yurii.pavlenko.myassistant.flights.logic.FlightSnapshot;
import com.yurii.pavlenko.myassistant.flights.logic.FlightStats;
import com.yurii.pavlenko.myassistant.flights.logic.GeoMath;
import com.yurii.pavlenko.myassistant.flights.logic.VerticalSpeed;
import com.yurii.pavlenko.myassistant.flights.sensor.GForceSensor;
import com.yurii.pavlenko.myassistant.flights.sensor.GpsTracker;
import com.yurii.pavlenko.myassistant.flights.sensor.HeadingSensor;

/**
 * Gathers the readings of the compass, the accelerometer and the GPS into one picture of the flight.
 * One instance serves the whole app: the screen watches it, and the recording service keeps it
 * running with the screen off. Everything happens on the main thread.
 */
public final class FlightRecorder {

    private static final double GRAVITY_WEIGHT = 0.25;
    private static final double HEADING_WEIGHT = 0.15;
    private static final long PUBLISH_INTERVAL_MILLIS = 100;

    private static FlightRecorder instance;

    private final Context context;
    private final MutableLiveData<FlightSnapshot> snapshot = new MutableLiveData<>(FlightSnapshot.idle());
    private final HeadingSensor headingSensor;
    private final GForceSensor gForceSensor;
    private final GpsTracker gps;
    private final AngleSmoother headingSmoother = new AngleSmoother(HEADING_WEIGHT);
    private final VerticalSpeed verticalSpeed = new VerticalSpeed();

    private boolean watching;
    private boolean recording;
    private boolean sensorsRunning;
    private boolean hasFix;
    private double magneticHeading = Double.NaN;
    private double declination;
    private double gForce = 1;
    private double gPeak = 1;
    private double speed;
    private double altitude;
    private FlightStats stats = FlightStats.empty();
    private long lastPublish;

    private FlightRecorder(Context context) {
        this.context = context.getApplicationContext();
        headingSensor = new HeadingSensor(this.context, this::onHeading);
        gForceSensor = new GForceSensor(this.context, this::onGForce);
        gps = new GpsTracker(this.context, this::onFix);
    }

    public static synchronized FlightRecorder get(Context context) {
        if (instance == null) {
            instance = new FlightRecorder(context);
        }
        return instance;
    }

    public LiveData<FlightSnapshot> getSnapshot() {
        return snapshot;
    }

    public boolean hasCompass() {
        return headingSensor.isAvailable();
    }

    /** The compass and the load meter run while the screen is in front or a flight is recorded. */
    public void setWatching(boolean watching) {
        this.watching = watching;
        updateSensors();
    }

    /** Starts a new recording; returns false when GPS cannot be used. */
    public boolean startRecording() {
        if (recording) {
            return true;
        }
        stats = FlightStats.empty();
        verticalSpeed.reset();
        hasFix = false;
        speed = 0;
        altitude = 0;
        gPeak = gForce;
        if (!gps.start()) {
            publish(true);
            return false;
        }
        recording = true;
        updateSensors();
        publish(true);
        return true;
    }

    public void stopRecording() {
        if (!recording) {
            return;
        }
        recording = false;
        gps.stop();
        updateSensors();
        publish(true);
    }

    private void updateSensors() {
        boolean needed = watching || recording;
        if (needed && !sensorsRunning) {
            headingSensor.start();
            gForceSensor.start();
        } else if (!needed && sensorsRunning) {
            headingSensor.stop();
            gForceSensor.stop();
        }
        sensorsRunning = needed;
    }

    private void onHeading(double degrees) {
        magneticHeading = headingSmoother.add(degrees);
        publish(false);
    }

    private void onGForce(double measured) {
        gForce += GRAVITY_WEIGHT * (measured - gForce);
        gPeak = Math.max(gPeak, gForce);
        publish(false);
    }

    private void onFix(Location location) {
        double altitudeMetres = altitudeOf(location);
        declination = new GeomagneticField((float) location.getLatitude(), (float) location.getLongitude(),
                (float) altitudeMetres, location.getTime()).getDeclination();
        speed = location.hasSpeed() ? location.getSpeed() : 0;
        altitude = altitudeMetres;
        verticalSpeed.add(location.getTime(), altitudeMetres);
        stats = stats.plus(new FlightPoint(location.getTime(), location.getLatitude(), location.getLongitude(),
                altitudeMetres, speed), gPeak);
        gPeak = gForce;
        hasFix = true;
        publish(true);
    }

    /** Height above mean sea level where the phone can tell it, otherwise above the GPS ellipsoid. */
    private static double altitudeOf(Location location) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && location.hasMslAltitude()) {
            return location.getMslAltitudeMeters();
        }
        return location.hasAltitude() ? location.getAltitude() : 0;
    }

    private void publish(boolean force) {
        long now = SystemClock.uptimeMillis();
        if (!force && now - lastPublish < PUBLISH_INTERVAL_MILLIS) {
            return;
        }
        lastPublish = now;
        double heading = Double.isNaN(magneticHeading) ? Double.NaN
                : GeoMath.normalizeDegrees(magneticHeading + declination);
        snapshot.setValue(new FlightSnapshot(recording, hasFix, heading, speed, altitude,
                verticalSpeed.metresPerSecond(), gForce, stats));
    }
}
