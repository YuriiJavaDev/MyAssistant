package com.yurii.pavlenko.myassistant.flights.logic;

import java.util.Locale;

/** Writes readings of the flight as text, in the units the user has chosen. */
public final class FlightFormat {

    private static final double SECONDS_PER_MINUTE = 60;

    private FlightFormat() {
    }

    public static String speed(double metresPerSecond, SpeedUnit unit) {
        return String.format(Locale.US, "%.0f %s", unit.fromMetresPerSecond(metresPerSecond), unit.symbol());
    }

    public static String altitude(double metres, AltitudeUnit unit) {
        return String.format(Locale.US, "%.0f %s", unit.fromMetres(metres), unit.symbol());
    }

    public static String distance(double metres, SpeedUnit unit) {
        double value = unit.distanceFromMetres(metres);
        return String.format(Locale.US, value < 10 ? "%.2f %s" : "%.1f %s", value, unit.distanceSymbol());
    }

    /** Climb rate with a sign; in metres per second, or in feet per minute when altitudes are in feet. */
    public static String climbRate(double metresPerSecond, AltitudeUnit unit) {
        if (unit == AltitudeUnit.FEET) {
            return String.format(Locale.US, "%+.0f ft/min", unit.fromMetres(metresPerSecond) * SECONDS_PER_MINUTE);
        }
        return String.format(Locale.US, "%+.1f m/s", metresPerSecond);
    }

    public static String gForce(double g) {
        return String.format(Locale.US, "%.2f G", g);
    }
}
