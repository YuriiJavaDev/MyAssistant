package com.yurii.pavlenko.myassistant.flights.data;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.os.Build;
import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yurii.pavlenko.myassistant.flights.audio.VarioAudio;
import com.yurii.pavlenko.myassistant.flights.logic.AltitudeEstimate;
import com.yurii.pavlenko.myassistant.flights.logic.AngleSmoother;
import com.yurii.pavlenko.myassistant.flights.logic.BaroVario;
import com.yurii.pavlenko.myassistant.flights.logic.FlightPoint;
import com.yurii.pavlenko.myassistant.flights.logic.FlightSnapshot;
import com.yurii.pavlenko.myassistant.flights.logic.FlightStats;
import com.yurii.pavlenko.myassistant.flights.logic.GeoMath;
import com.yurii.pavlenko.myassistant.flights.logic.Orientation;
import com.yurii.pavlenko.myassistant.flights.logic.VarioSoundSettings;
import com.yurii.pavlenko.myassistant.flights.logic.VarioTone;
import com.yurii.pavlenko.myassistant.flights.logic.VerticalSpeed;
import com.yurii.pavlenko.myassistant.flights.sensor.BarometerSensor;
import com.yurii.pavlenko.myassistant.flights.sensor.GForceSensor;
import com.yurii.pavlenko.myassistant.flights.sensor.GpsTracker;
import com.yurii.pavlenko.myassistant.flights.sensor.OrientationSensor;

/**
 * Gathers the readings of the compass, the barometer, the accelerometer and the GPS into one picture
 * of the flight. One instance serves the whole app: the screen watches it, and the recording service
 * keeps it running with the screen off. Everything happens on the main thread.
 */
public final class FlightRecorder {

    private static final double GRAVITY_WEIGHT = 0.25;
    private static final double ANGLE_WEIGHT = 0.2;
    private static final long PUBLISH_INTERVAL_MILLIS = 100;

    private static FlightRecorder instance;

    private final MutableLiveData<FlightSnapshot> snapshot = new MutableLiveData<>(FlightSnapshot.idle());
    private final OrientationSensor orientationSensor;
    private final BarometerSensor barometer;
    private final GForceSensor gForceSensor;
    private final GpsTracker gps;
    private final VarioSoundStore soundStore;
    private final FlightRepository repository;
    private final VarioAudio audio = new VarioAudio();
    private final AngleSmoother headingSmoother = new AngleSmoother(ANGLE_WEIGHT);
    private final AngleSmoother pitchSmoother = new AngleSmoother(ANGLE_WEIGHT);
    private final AngleSmoother rollSmoother = new AngleSmoother(ANGLE_WEIGHT);
    private final BaroVario baroVario = new BaroVario();
    private final VerticalSpeed gpsVario = new VerticalSpeed();
    private final AltitudeEstimate altitudeEstimate = new AltitudeEstimate();

    private VarioSoundSettings soundSettings;
    private boolean watching;
    private boolean recording;
    private boolean sensorsRunning;
    private boolean gpsRunning;
    private boolean hasFix;
    private Orientation orientation = Orientation.unknown();
    private double declination;
    private double gForce = 1;
    private double gPeak = 1;
    private double speed;
    private double altitudeReference = Double.NaN;
    private FlightStats stats = FlightStats.empty();
    private long lastPublish;

    private FlightRecorder(Context context) {
        orientationSensor = new OrientationSensor(context, this::onOrientation);
        barometer = new BarometerSensor(context, this::onPressure);
        gForceSensor = new GForceSensor(context, this::onGForce);
        gps = new GpsTracker(context, this::onFix);
        soundStore = new VarioSoundStore(context);
        soundSettings = soundStore.load();
        repository = FlightRepository.get(context);
        repository.recoverUnfinished();
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

    public VarioSoundSettings getSoundSettings() {
        return soundSettings;
    }

    public void setSoundSettings(VarioSoundSettings settings) {
        soundSettings = settings;
        soundStore.save(settings);
        updateSensors();
        publish(true);
    }

    /** The instruments run while the screen is in front or a flight is recorded. */
    public void setWatching(boolean watching) {
        this.watching = watching;
        updateSensors();
    }

    /** Starts a new recording; returns false when GPS cannot be used. */
    public boolean startRecording() {
        if (recording) {
            return true;
        }
        if (!gpsRunning) {
            gpsRunning = gps.start();
            if (!gpsRunning) {
                publish(true);
                return false;
            }
        }
        stats = FlightStats.empty();
        hasFix = false;
        gPeak = gForce;
        recording = true;
        repository.beginFlight(System.currentTimeMillis(), altitudeFromStart());
        updateSensors();
        publish(true);
        return true;
    }

    public void stopRecording() {
        if (!recording) {
            return;
        }
        recording = false;
        repository.finishFlight(stats, altitudeFromStart());
        updateSensors();
        publish(true);
    }

    /** From now on altitudes are counted from the current one, as the pilot does at the start. */
    public void zeroAltitude() {
        altitudeReference = altitudeEstimate.metres();
        stats = stats.withAltitudeRestarted(0);
        publish(true);
    }

    /** Back to altitudes above sea level. */
    public void clearAltitudeZero() {
        altitudeReference = Double.NaN;
        stats = stats.withAltitudeRestarted(altitudeEstimate.metres());
        publish(true);
    }

    private void updateSensors() {
        boolean needed = watching || recording;
        if (needed && !sensorsRunning) {
            baroVario.reset();
            gpsVario.reset();
            orientationSensor.start();
            gForceSensor.start();
            barometer.start();
        } else if (!needed && sensorsRunning) {
            orientationSensor.stop();
            gForceSensor.stop();
            barometer.stop();
            altitudeEstimate.barometerPaused();
        }
        sensorsRunning = needed;
        updateGps();
        updateAudio();
    }

    private void updateGps() {
        if (sensorsRunning && !gpsRunning) {
            gpsRunning = gps.start();
        } else if (!sensorsRunning && gpsRunning) {
            gps.stop();
            gpsRunning = false;
        }
    }

    private void updateAudio() {
        if (sensorsRunning && soundSettings.isEnabled()) {
            audio.start();
        } else {
            audio.stop();
        }
    }

    private void onOrientation(Orientation measured) {
        double heading = headingSmoother.add(measured.getHeading());
        double pitch = signed(pitchSmoother.add(measured.getPitch()));
        double roll = signed(rollSmoother.add(measured.getRoll()));
        orientation = new Orientation(heading, pitch, roll);
        publish(false);
    }

    private static double signed(double degrees) {
        return GeoMath.turnBetween(0, degrees);
    }

    private void onPressure(long timeMillis, double hectopascals) {
        baroVario.add(timeMillis, hectopascals);
        altitudeEstimate.onBarometer(baroVario.altitudeMetres());
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
        altitudeEstimate.onGps(altitudeMetres);
        gpsVario.add(location.getTime(), altitudeMetres);
        hasFix = true;
        if (recording) {
            FlightPoint point = new FlightPoint(location.getTime(), location.getLatitude(), location.getLongitude(),
                    shownAltitude(), speed, climbRate());
            stats = stats.plus(point, gPeak);
            repository.appendPoint(point, gPeak);
            gPeak = gForce;
        }
        publish(true);
    }

    /** Height above mean sea level where the phone can tell it, otherwise above the GPS ellipsoid. */
    private static double altitudeOf(Location location) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && location.hasMslAltitude()) {
            return location.getMslAltitudeMeters();
        }
        return location.hasAltitude() ? location.getAltitude() : 0;
    }

    private boolean altitudeFromStart() {
        return !Double.isNaN(altitudeReference);
    }

    private double shownAltitude() {
        return altitudeEstimate.metres() - (altitudeFromStart() ? altitudeReference : 0);
    }

    private double climbRate() {
        return baroVario.hasReading() ? baroVario.metresPerSecond() : gpsVario.metresPerSecond();
    }

    private void publish(boolean force) {
        long now = SystemClock.uptimeMillis();
        if (!force && now - lastPublish < PUBLISH_INTERVAL_MILLIS) {
            return;
        }
        lastPublish = now;
        double climb = climbRate();
        if (sensorsRunning && soundSettings.isEnabled()) {
            audio.setTone(VarioTone.of(climb, soundSettings));
        }
        double heading = Double.isNaN(orientation.getHeading()) ? Double.NaN
                : GeoMath.normalizeDegrees(orientation.getHeading() + declination);
        snapshot.setValue(new FlightSnapshot(recording, hasFix,
                new Orientation(heading, orientation.getPitch(), orientation.getRoll()),
                speed, shownAltitude(), altitudeFromStart(), climb, gForce, stats));
    }
}
