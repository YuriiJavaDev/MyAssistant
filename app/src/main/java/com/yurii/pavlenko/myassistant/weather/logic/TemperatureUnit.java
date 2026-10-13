package com.yurii.pavlenko.myassistant.weather.logic;

import java.util.Locale;

/** How temperatures are shown; the weather service reports degrees Celsius. */
public enum TemperatureUnit {
    CELSIUS("°C") {
        @Override
        double convert(double celsius) {
            return celsius;
        }
    },
    FAHRENHEIT("°F") {
        @Override
        double convert(double celsius) {
            return celsius * 9 / 5 + 32;
        }
    };

    private final String symbol;

    TemperatureUnit(String symbol) {
        this.symbol = symbol;
    }

    abstract double convert(double celsius);

    /** Whole degrees with the unit, for example "21°C". */
    public String format(double celsius) {
        return String.format(Locale.US, "%d%s", Math.round(convert(celsius)), symbol);
    }

    /** Whole degrees without the unit, for ranges such as "24° / 17°". */
    public String formatShort(double celsius) {
        return String.format(Locale.US, "%d°", Math.round(convert(celsius)));
    }
}
