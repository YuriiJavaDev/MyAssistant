package com.yurii.pavlenko.myassistant.weather.data;

import com.yurii.pavlenko.myassistant.weather.logic.TemperatureUnit;
import com.yurii.pavlenko.myassistant.weather.logic.WindUnit;

/** The units the user chose for temperature and wind. */
public final class WeatherUnits {

    private final TemperatureUnit temperature;
    private final WindUnit wind;

    public WeatherUnits(TemperatureUnit temperature, WindUnit wind) {
        this.temperature = temperature;
        this.wind = wind;
    }

    public TemperatureUnit getTemperature() {
        return temperature;
    }

    public WindUnit getWind() {
        return wind;
    }
}
