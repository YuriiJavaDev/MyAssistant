package com.yurii.pavlenko.myassistant.flights.logic;

/** Smooths a heading that jitters, correctly across the jump from 359 to 0 degrees. */
public final class AngleSmoother {

    private final double weight;
    private double value;
    private boolean started;

    /** @param weight how much of each new reading is taken, from above 0 up to 1 */
    public AngleSmoother(double weight) {
        this.weight = weight;
    }

    public double add(double degrees) {
        if (!started) {
            started = true;
            value = GeoMath.normalizeDegrees(degrees);
        } else {
            value = GeoMath.normalizeDegrees(value + weight * GeoMath.turnBetween(value, degrees));
        }
        return value;
    }
}
