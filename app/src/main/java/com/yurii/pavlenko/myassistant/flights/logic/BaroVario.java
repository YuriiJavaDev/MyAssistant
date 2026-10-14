package com.yurii.pavlenko.myassistant.flights.logic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/** Altitude and climb rate from the barometer, which reacts within a second where GPS needs ten. */
public final class BaroVario {

    private static final double SEA_LEVEL_HECTOPASCALS = 1013.25;
    private static final double SCALE_METRES = 44_330;
    private static final double PRESSURE_EXPONENT = 1 / 5.255;
    private static final double SMOOTHING_SECONDS = 0.4;
    private static final double WINDOW_SECONDS = 1.0;
    private static final double OUTPUT_SECONDS = 0.5;
    private static final double MIN_SPAN_SECONDS = 0.5;
    private static final int TIME = 0;
    private static final int ALTITUDE = 1;

    private final Deque<double[]> history = new ArrayDeque<>();
    private double smoothed = Double.NaN;
    private double climbRate;

    /** The altitude in metres for a pressure, in the standard atmosphere. */
    public static double altitudeOf(double hectopascals) {
        return SCALE_METRES * (1 - Math.pow(hectopascals / SEA_LEVEL_HECTOPASCALS, PRESSURE_EXPONENT));
    }

    public void add(long timeMillis, double hectopascals) {
        double altitude = altitudeOf(hectopascals);
        if (history.isEmpty()) {
            smoothed = altitude;
        } else {
            double seconds = Math.max(0, (timeMillis - history.peekLast()[TIME]) / 1000.0);
            smoothed += weight(seconds, SMOOTHING_SECONDS) * (altitude - smoothed);
        }
        double seconds = history.isEmpty() ? 0 : (timeMillis - history.peekLast()[TIME]) / 1000.0;
        history.addLast(new double[]{timeMillis, smoothed});
        while (history.size() > 2 && timeMillis - second()[TIME] >= WINDOW_SECONDS * 1000) {
            history.removeFirst();
        }
        double[] oldest = history.peekFirst();
        double span = (timeMillis - oldest[TIME]) / 1000.0;
        if (span >= MIN_SPAN_SECONDS) {
            double slope = (smoothed - oldest[ALTITUDE]) / span;
            climbRate += weight(seconds, OUTPUT_SECONDS) * (slope - climbRate);
        }
    }

    private double[] second() {
        Iterator<double[]> iterator = history.iterator();
        iterator.next();
        return iterator.next();
    }

    private static double weight(double seconds, double timeConstant) {
        return 1 - Math.exp(-seconds / timeConstant);
    }

    public boolean hasReading() {
        return !history.isEmpty();
    }

    /** The smoothed altitude in metres, or NaN before the first reading. */
    public double altitudeMetres() {
        return smoothed;
    }

    /** Metres per second, positive when climbing. */
    public double metresPerSecond() {
        return climbRate;
    }

    public void reset() {
        history.clear();
        smoothed = Double.NaN;
        climbRate = 0;
    }
}
