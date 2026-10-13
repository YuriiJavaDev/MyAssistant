package com.yurii.pavlenko.myassistant.weather.data;

/** Weather right now, with today's temperature range. Speeds are in metres per second. */
public final class CurrentWeather {

    private final double temperature;
    private final double feelsLike;
    private final int humidity;
    private final double windSpeed;
    private final int weatherCode;
    private final boolean day;
    private final double todayMax;
    private final double todayMin;

    public CurrentWeather(double temperature, double feelsLike, int humidity, double windSpeed,
                          int weatherCode, boolean day, double todayMax, double todayMin) {
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.weatherCode = weatherCode;
        this.day = day;
        this.todayMax = todayMax;
        this.todayMin = todayMin;
    }

    public double getTemperature() { return temperature; }
    public double getFeelsLike() { return feelsLike; }
    public int getHumidity() { return humidity; }
    public double getWindSpeed() { return windSpeed; }
    public int getWeatherCode() { return weatherCode; }
    public boolean isDay() { return day; }
    public double getTodayMax() { return todayMax; }
    public double getTodayMin() { return todayMin; }
}
