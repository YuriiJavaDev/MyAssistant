package com.yurii.pavlenko.myassistant.flights.logic;

/** One position fix of a flight. */
public final class FlightPoint {

    private final long timeMillis;
    private final double latitude;
    private final double longitude;
    private final double altitudeMetres;
    private final double speedMetresPerSecond;
    private final double climbMetresPerSecond;

    public FlightPoint(long timeMillis, double latitude, double longitude,
                       double altitudeMetres, double speedMetresPerSecond, double climbMetresPerSecond) {
        this.timeMillis = timeMillis;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitudeMetres = altitudeMetres;
        this.speedMetresPerSecond = speedMetresPerSecond;
        this.climbMetresPerSecond = climbMetresPerSecond;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitudeMetres() {
        return altitudeMetres;
    }

    public double getSpeedMetresPerSecond() {
        return speedMetresPerSecond;
    }

    /** Metres per second, positive when climbing. */
    public double getClimbMetresPerSecond() {
        return climbMetresPerSecond;
    }
}
