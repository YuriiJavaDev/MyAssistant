package com.yurii.pavlenko.myassistant.weather.viewmodel;

import com.yurii.pavlenko.myassistant.weather.data.CurrentWeather;
import com.yurii.pavlenko.myassistant.weather.data.Place;

/** What the weather screen shows: loading, a result, or the reason there is none. */
public final class WeatherState {

    public enum Status { LOADING, CONTENT, NEEDS_LOCATION_PERMISSION, ERROR }

    private final Status status;
    private final Place place;
    private final CurrentWeather weather;
    private final String message;

    private WeatherState(Status status, Place place, CurrentWeather weather, String message) {
        this.status = status;
        this.place = place;
        this.weather = weather;
        this.message = message;
    }

    static WeatherState loading() {
        return new WeatherState(Status.LOADING, null, null, null);
    }

    static WeatherState content(Place place, CurrentWeather weather) {
        return new WeatherState(Status.CONTENT, place, weather, null);
    }

    static WeatherState needsLocationPermission() {
        return new WeatherState(Status.NEEDS_LOCATION_PERMISSION, null, null, null);
    }

    static WeatherState error(String message) {
        return new WeatherState(Status.ERROR, null, null, message);
    }

    public Status getStatus() { return status; }
    public Place getPlace() { return place; }
    public CurrentWeather getWeather() { return weather; }
    public String getMessage() { return message; }
}
