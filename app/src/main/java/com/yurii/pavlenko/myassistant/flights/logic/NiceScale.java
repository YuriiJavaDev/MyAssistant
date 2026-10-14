package com.yurii.pavlenko.myassistant.flights.logic;

/** Picks the upper end of a dial from the values 1, 2 and 5 times a power of ten. */
public final class NiceScale {

    private static final double[] STEPS = {1, 2, 5};
    private static final double MIN = 10;

    private NiceScale() {
    }

    /** The smallest round number, at least 10, that is not below the value. */
    public static double maxFor(double value) {
        double magnitude = 1;
        while (true) {
            for (double step : STEPS) {
                double candidate = step * magnitude;
                if (candidate >= MIN && candidate >= value) {
                    return candidate;
                }
            }
            magnitude *= 10;
        }
    }
}
