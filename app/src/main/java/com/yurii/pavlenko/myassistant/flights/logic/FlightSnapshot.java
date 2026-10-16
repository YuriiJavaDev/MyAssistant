package com.yurii.pavlenko.myassistant.flights.logic;

/** Everything the instruments show at one moment. */
public final class FlightSnapshot {

    private final boolean recording;
    private final boolean hasFix;
    private final Orientation orientation;
    private final double speed;
    private final double altitude;
    private final boolean altitudeFromStart;
    private final double verticalSpeed;
    private final double gForce;
    private final FlightStats stats;

    public FlightSnapshot(boolean recording, boolean hasFix, Orientation orientation, double speed,
                          double altitude, boolean altitudeFromStart, double verticalSpeed,
                          double gForce, FlightStats stats) {
        this.recording = recording;
        this.hasFix = hasFix;
        this.orientation = orientation;
        this.speed = speed;
        this.altitude = altitude;
        this.altitudeFromStart = altitudeFromStart;
        this.verticalSpeed = verticalSpeed;
        this.gForce = gForce;
        this.stats = stats;
    }

    public static FlightSnapshot idle() {
        return new FlightSnapshot(false, false, Orientation.unknown(), 0, 0, false, 0, 1, FlightStats.empty());
    }

    public boolean isRecording() {
        return recording;
    }

    /** Whether a GPS position has been received since the screen or the recording began. */
    public boolean hasFix() {
        return hasFix;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    /** Ground speed in metres per second. */
    public double getSpeed() {
        return speed;
    }

    /** Altitude in metres: above sea level, or above the start after the altitude was zeroed. */
    public double getAltitude() {
        return altitude;
    }

    public boolean isAltitudeFromStart() {
        return altitudeFromStart;
    }

    /** Climb rate in metres per second; negative when descending. */
    public double getVerticalSpeed() {
        return verticalSpeed;
    }

    /** The load on the phone in units of gravity; 1 when lying still. */
    public double getGForce() {
        return gForce;
    }

    public FlightStats getStats() {
        return stats;
    }
}
