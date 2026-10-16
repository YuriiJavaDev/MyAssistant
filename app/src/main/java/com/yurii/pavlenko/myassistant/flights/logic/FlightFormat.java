package com.yurii.pavlenko.myassistant.flights.logic;

import java.util.Locale;

/** Writes readings of the flight as text, in the units the user has chosen. */
public final class FlightFormat {

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

    /** Climb rate with a sign. */
    public static String climbRate(double metresPerSecond, ClimbUnit unit) {
        return String.format(Locale.US, "%+." + unit.decimals() + "f %s",
                unit.fromMetresPerSecond(metresPerSecond), unit.symbol());
    }

    /** A position as degrees with the side of the world, for example 32.8000°N 35.0500°E. */
    public static String position(FlightPoint point) {
        return String.format(Locale.US, "%.4f°%s %.4f°%s",
                Math.abs(point.getLatitude()), point.getLatitude() < 0 ? "S" : "N",
                Math.abs(point.getLongitude()), point.getLongitude() < 0 ? "W" : "E");
    }

    public static String gForce(double g) {
        return String.format(Locale.US, "%.2f G", g);
    }
}
