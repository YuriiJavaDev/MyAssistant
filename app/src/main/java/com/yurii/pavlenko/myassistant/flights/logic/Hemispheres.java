package com.yurii.pavlenko.myassistant.flights.logic;

/** The letters that name the sides of the world next to a coordinate. */
public final class Hemispheres {

    private final String north;
    private final String south;
    private final String east;
    private final String west;

    public Hemispheres(String north, String south, String east, String west) {
        this.north = north;
        this.south = south;
        this.east = east;
        this.west = west;
    }

    String latitude(double degrees) {
        return degrees < 0 ? south : north;
    }

    String longitude(double degrees) {
        return degrees < 0 ? west : east;
    }
}
