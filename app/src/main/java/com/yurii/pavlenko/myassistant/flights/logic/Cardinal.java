package com.yurii.pavlenko.myassistant.flights.logic;

/** Names a heading by the nearest of the eight compass points. */
public final class Cardinal {

    private static final String[] POINTS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    private static final double SECTOR = 45;

    private Cardinal() {
    }

    public static String of(double degrees) {
        int index = (int) Math.round(GeoMath.normalizeDegrees(degrees) / SECTOR) % POINTS.length;
        return POINTS[index];
    }
}
