package com.yurii.pavlenko.myassistant.flights.logic;

import java.util.ArrayDeque;
import java.util.Deque;

/** Climb or descent rate from the altitude readings of the last few seconds. */
public final class VerticalSpeed {

    private static final long WINDOW_MILLIS = 10_000;
    private static final long MIN_SPAN_MILLIS = 3_000;
    private static final int TIME = 0;
    private static final int ALTITUDE = 1;

    private final Deque<double[]> readings = new ArrayDeque<>();

    public void add(long timeMillis, double altitudeMetres) {
        readings.addLast(new double[]{timeMillis, altitudeMetres});
        while (readings.size() > 1 && timeMillis - readings.peekFirst()[TIME] > WINDOW_MILLIS) {
            readings.removeFirst();
        }
    }

    /** Metres per second, positive when climbing; 0 until the readings span a few seconds. */
    public double metresPerSecond() {
        if (readings.size() < 2) {
            return 0;
        }
        double[] first = readings.peekFirst();
        double[] last = readings.peekLast();
        double span = last[TIME] - first[TIME];
        return span < MIN_SPAN_MILLIS ? 0 : (last[ALTITUDE] - first[ALTITUDE]) / (span / 1000);
    }

    public void reset() {
        readings.clear();
    }
}
