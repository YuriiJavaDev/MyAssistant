package com.yurii.pavlenko.myassistant.flights.logic;

/**
 * The best guess of the altitude above sea level: the barometer supplies the quick changes, GPS holds
 * the level steady and corrects the drift of the weather.
 */
public final class AltitudeEstimate {

    private static final double GPS_WEIGHT_WITH_BAROMETER = 0.05;
    private static final double GPS_WEIGHT_ALONE = 0.3;

    private double value = Double.NaN;
    private double lastBarometer = Double.NaN;
    private boolean gpsSeen;

    /** @param altitudeMetres the smoothed barometer altitude, in metres */
    public void onBarometer(double altitudeMetres) {
        if (Double.isNaN(value)) {
            value = altitudeMetres;
        } else if (!Double.isNaN(lastBarometer)) {
            value += altitudeMetres - lastBarometer;
        }
        lastBarometer = altitudeMetres;
    }

    /** The barometer was switched off for a while; its next reading starts a new comparison. */
    public void barometerPaused() {
        lastBarometer = Double.NaN;
    }

    public void onGps(double altitudeMetres) {
        if (Double.isNaN(value) || !gpsSeen) {
            value = altitudeMetres;
            gpsSeen = true;
        } else {
            double weight = Double.isNaN(lastBarometer) ? GPS_WEIGHT_ALONE : GPS_WEIGHT_WITH_BAROMETER;
            value += weight * (altitudeMetres - value);
        }
    }

    /** Metres above sea level; 0 before any reading. */
    public double metres() {
        return Double.isNaN(value) ? 0 : value;
    }
}
