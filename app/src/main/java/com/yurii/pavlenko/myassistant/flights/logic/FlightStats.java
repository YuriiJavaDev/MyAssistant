package com.yurii.pavlenko.myassistant.flights.logic;

/** Totals of a flight, built up one position fix at a time. */
public final class FlightStats {

    /** Movement slower than this is GPS noise on the ground, not travel. */
    private static final double MIN_TRAVEL_SPEED = 1.0;

    private final long startMillis;
    private final long endMillis;
    private final double distanceMetres;
    private final double maxSpeed;
    private final double maxAltitude;
    private final double maxGForce;
    private final FlightPoint last;

    private FlightStats(long startMillis, long endMillis, double distanceMetres, double maxSpeed,
                        double maxAltitude, double maxGForce, FlightPoint last) {
        this.startMillis = startMillis;
        this.endMillis = endMillis;
        this.distanceMetres = distanceMetres;
        this.maxSpeed = maxSpeed;
        this.maxAltitude = maxAltitude;
        this.maxGForce = maxGForce;
        this.last = last;
    }

    public static FlightStats empty() {
        return new FlightStats(0, 0, 0, 0, Double.NEGATIVE_INFINITY, 0, null);
    }

    /** Returns the totals including a new position fix and the load felt at that moment, in G. */
    public FlightStats plus(FlightPoint point, double gForce) {
        if (last == null) {
            return new FlightStats(point.getTimeMillis(), point.getTimeMillis(), 0,
                    point.getSpeedMetresPerSecond(), point.getAltitudeMetres(), gForce, point);
        }
        double segment = GeoMath.distanceMetres(last.getLatitude(), last.getLongitude(),
                point.getLatitude(), point.getLongitude());
        double seconds = Math.max(1, (point.getTimeMillis() - last.getTimeMillis()) / 1000.0);
        double travelled = segment / seconds >= MIN_TRAVEL_SPEED ? segment : 0;
        return new FlightStats(startMillis, point.getTimeMillis(), distanceMetres + travelled,
                Math.max(maxSpeed, point.getSpeedMetresPerSecond()),
                Math.max(maxAltitude, point.getAltitudeMetres()),
                Math.max(maxGForce, gForce), point);
    }

    /** The same totals with the highest altitude counted again from the current one, after a new zero. */
    public FlightStats withAltitudeRestarted(double currentAltitude) {
        return last == null ? this : new FlightStats(startMillis, endMillis, distanceMetres, maxSpeed,
                currentAltitude, maxGForce, last);
    }

    public boolean isEmpty() {
        return last == null;
    }

    public long getStartMillis() {
        return startMillis;
    }

    public long getEndMillis() {
        return endMillis;
    }

    public long getDurationMillis() {
        return endMillis - startMillis;
    }

    public double getDistanceMetres() {
        return distanceMetres;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    /** Average speed over the whole time of the flight, in metres per second. */
    public double getAverageSpeed() {
        long millis = getDurationMillis();
        return millis <= 0 ? 0 : distanceMetres / (millis / 1000.0);
    }

    /** The highest altitude reached, or 0 before the first fix. */
    public double getMaxAltitude() {
        return last == null ? 0 : maxAltitude;
    }

    public double getMaxGForce() {
        return maxGForce;
    }
}
