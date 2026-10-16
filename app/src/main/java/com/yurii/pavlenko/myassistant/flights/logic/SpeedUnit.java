package com.yurii.pavlenko.myassistant.flights.logic;

/** The unit in which speeds are shown. */
public enum SpeedUnit {
    KILOMETRES_PER_HOUR(3.6, "km/h", 1000, "km"),
    KNOTS(1.943844, "kt", 1852, "nm"),
    METRES_PER_SECOND(1, "m/s", 1000, "km");

    private final double perMetrePerSecond;
    private final String symbol;
    private final double metresPerDistanceUnit;
    private final String distanceSymbol;

    SpeedUnit(double perMetrePerSecond, String symbol, double metresPerDistanceUnit, String distanceSymbol) {
        this.perMetrePerSecond = perMetrePerSecond;
        this.symbol = symbol;
        this.metresPerDistanceUnit = metresPerDistanceUnit;
        this.distanceSymbol = distanceSymbol;
    }

    public double fromMetresPerSecond(double speed) {
        return speed * perMetrePerSecond;
    }

    public String symbol() {
        return symbol;
    }

    /** Distances go with the speed: kilometres, or nautical miles for knots. */
    public double distanceFromMetres(double metres) {
        return metres / metresPerDistanceUnit;
    }

    public String distanceSymbol() {
        return distanceSymbol;
    }
}
