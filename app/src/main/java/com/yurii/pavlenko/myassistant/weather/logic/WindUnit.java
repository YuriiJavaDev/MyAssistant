package com.yurii.pavlenko.myassistant.weather.logic;

import java.util.Locale;

/** How wind speed is shown; the weather service reports metres per second. */
public enum WindUnit {
    METRES_PER_SECOND("m/s", 1.0),
    KILOMETRES_PER_HOUR("km/h", 3.6);

    private final String symbol;
    private final double perMetrePerSecond;

    WindUnit(String symbol, double perMetrePerSecond) {
        this.symbol = symbol;
        this.perMetrePerSecond = perMetrePerSecond;
    }

    /** One decimal with the unit, for example "3.2 m/s". */
    public String format(double metresPerSecond) {
        return String.format(Locale.US, "%.1f %s", metresPerSecond * perMetrePerSecond, symbol);
    }
}
