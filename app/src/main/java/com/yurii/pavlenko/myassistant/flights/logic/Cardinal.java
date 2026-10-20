package com.yurii.pavlenko.myassistant.flights.logic;

/** Names a heading by the nearest of the eight compass points, counted clockwise from north. */
public final class Cardinal {

    public static final int POINT_COUNT = 8;
    private static final double SECTOR = 45;

    private Cardinal() {
    }

    /** 0 for north, 1 for north-east, and so on round to 7 for north-west. */
    public static int indexOf(double degrees) {
        return (int) Math.round(GeoMath.normalizeDegrees(degrees) / SECTOR) % POINT_COUNT;
    }
}
