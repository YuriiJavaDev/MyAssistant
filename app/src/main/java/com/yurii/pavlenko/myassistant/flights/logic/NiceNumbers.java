package com.yurii.pavlenko.myassistant.flights.logic;

import java.util.ArrayList;
import java.util.List;

/** Round numbers for the grid lines of the map, the scale bar and the axes of the charts. */
public final class NiceNumbers {

    private NiceNumbers() {
    }

    /** The smallest of 1, 2, 5, 10, 20, 50... that is not less than the given positive number. */
    public static double roundUp(double raw) {
        double magnitude = Math.pow(10, Math.floor(Math.log10(raw)));
        double fraction = raw / magnitude;
        double nice = fraction <= 1 ? 1 : fraction <= 2 ? 2 : fraction <= 5 ? 5 : 10;
        return nice * magnitude;
    }

    /** The largest of 1, 2, 5, 10, 20, 50... that does not exceed the given positive number. */
    public static double roundDown(double raw) {
        double magnitude = Math.pow(10, Math.floor(Math.log10(raw)));
        double fraction = raw / magnitude;
        double nice = fraction >= 5 ? 5 : fraction >= 2 ? 2 : 1;
        return nice * magnitude;
    }

    /** Round values from the start of the range to its end, about the given number of them. */
    public static List<Double> ticks(double min, double max, int approximateCount) {
        List<Double> ticks = new ArrayList<>();
        if (max <= min) {
            ticks.add(min);
            return ticks;
        }
        double step = roundUp((max - min) / approximateCount);
        double first = Math.ceil(min / step - 1e-9) * step;
        for (double tick = first; tick <= max + step * 1e-9; tick += step) {
            ticks.add(Math.round(tick / step) * step);
        }
        return ticks;
    }
}
