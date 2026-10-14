package com.yurii.pavlenko.myassistant.flights.logic;

/** Everything the instruments show at one moment. */
public final class FlightSnapshot {

    private final boolean recording;
    private final boolean hasFix;
    private final double heading;
    private final double speed;
    private final double altitude;
    private final double verticalSpeed;
    private final double gForce;
    private final FlightStats stats;

    public FlightSnapshot(boolean recording, boolean hasFix, double heading, double speed,
                          double altitude, double verticalSpeed, double gForce, FlightStats stats) {
        this.recording = recording;
        this.hasFix = hasFix;
        this.heading = heading;
        this.speed = speed;
        this.altitude = altitude;
        this.verticalSpeed = verticalSpeed;
        this.gForce = gForce;
        this.stats = stats;
    }

    public static FlightSnapshot idle() {
        return new FlightSnapshot(false, false, Double.NaN, 0, 0, 0, 1, FlightStats.empty());
    }

    public boolean isRecording() {
        return recording;
    }

    /** Whether a GPS position has been received since the recording began. */
    public boolean hasFix() {
        return hasFix;
    }

    /** The heading in degrees, or NaN when the phone has no compass. */
    public double getHeading() {
        return heading;
    }

    public boolean hasHeading() {
        return !Double.isNaN(heading);
    }

    /** Ground speed in metres per second. */
    public double getSpeed() {
        return speed;
    }

    /** Altitude in metres. */
    public double getAltitude() {
        return altitude;
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
