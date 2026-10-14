package com.yurii.pavlenko.myassistant.flights.logic;

/** The unit in which altitudes are shown. */
public enum AltitudeUnit {
    METRES(1, "m"),
    FEET(3.280840, "ft");

    private final double perMetre;
    private final String symbol;

    AltitudeUnit(double perMetre, String symbol) {
        this.perMetre = perMetre;
        this.symbol = symbol;
    }

    public double fromMetres(double metres) {
        return metres * perMetre;
    }

    public String symbol() {
        return symbol;
    }
}
