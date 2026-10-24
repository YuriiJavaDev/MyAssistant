package com.yurii.pavlenko.myassistant.weather.data;

/** One hour of a forecast: the values of the service for a moment, before they are turned into the weather shown. */
public final class ForecastSample {

    final long timeMillis;
    final double temperature;
    final double humidity;
    final double windSpeed;
    /** The service's symbol for the following hour, such as "rain_day"; empty when it gave none. */
    final String symbol;

    public ForecastSample(long timeMillis, double temperature, double humidity, double windSpeed, String symbol) {
        this.timeMillis = timeMillis;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.symbol = symbol;
    }
}
