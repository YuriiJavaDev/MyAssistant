package com.yurii.pavlenko.myassistant.flights.logic;

import java.util.Locale;

/** Writes a length of time the way a flight timer shows it. */
public final class FlightTime {

    private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;

    private FlightTime() {
    }

    /** For example 1:05:09. */
    public static String format(long millis) {
        long safe = Math.max(0, millis);
        return String.format(Locale.US, "%d:%02d:%02d", safe / HOUR, safe % HOUR / MINUTE, safe % MINUTE / SECOND);
    }
}
