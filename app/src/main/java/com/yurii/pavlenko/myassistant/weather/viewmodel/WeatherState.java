package com.yurii.pavlenko.myassistant.weather.viewmodel;

import com.yurii.pavlenko.myassistant.weather.data.CurrentWeather;
import com.yurii.pavlenko.myassistant.weather.data.Place;

/** What the weather screen shows: loading, a result, or the reason there is none. */
public final class WeatherState {

    public enum Status { LOADING, CONTENT, NEEDS_LOCATION_PERMISSION, ERROR }

    /** Why there is no weather; the screen words it. */
    public enum Error { NO_LOCATION, CITY_NOT_FOUND, NO_NETWORK, CHOOSE_CITY, CITY_LIST_UNREADABLE }

    private final Status status;
    private final Place place;
    private final CurrentWeather weather;
    private final Error error;
    private final String errorArgument;

    private WeatherState(Status status, Place place, CurrentWeather weather, Error error, String errorArgument) {
        this.status = status;
        this.place = place;
        this.weather = weather;
        this.error = error;
        this.errorArgument = errorArgument;
    }

    static WeatherState loading() {
        return new WeatherState(Status.LOADING, null, null, null, null);
    }

    static WeatherState content(Place place, CurrentWeather weather) {
        return new WeatherState(Status.CONTENT, place, weather, null, null);
    }

    static WeatherState needsLocationPermission() {
        return new WeatherState(Status.NEEDS_LOCATION_PERMISSION, null, null, null, null);
    }

    static WeatherState error(Error error) {
        return error(error, null);
    }

    static WeatherState error(Error error, String argument) {
        return new WeatherState(Status.ERROR, null, null, error, argument);
    }

    public Status getStatus() { return status; }
    public Place getPlace() { return place; }
    public CurrentWeather getWeather() { return weather; }
    public Error getError() { return error; }
    /** The text the error is about, such as the city that was searched for; null when there is none. */
    public String getErrorArgument() { return errorArgument; }
}
