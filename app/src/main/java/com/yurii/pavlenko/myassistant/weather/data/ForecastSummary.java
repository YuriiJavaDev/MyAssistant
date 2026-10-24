package com.yurii.pavlenko.myassistant.weather.data;

import com.yurii.pavlenko.myassistant.weather.logic.FeelsLike;
import com.yurii.pavlenko.myassistant.weather.logic.MetSymbol;
import com.yurii.pavlenko.myassistant.weather.logic.Sun;

import java.util.List;

/** Turns the hours of a forecast into the weather right now, with the range of the next day. */
public final class ForecastSummary {

    private static final long DAY_MILLIS = 24L * 60 * 60 * 1000;

    private ForecastSummary() {
    }

    /** @param hours the forecast hours in time order, the first being the current one; must not be empty */
    public static CurrentWeather of(List<ForecastSample> hours, double latitude, double longitude) {
        ForecastSample now = hours.get(0);
        double max = now.temperature;
        double min = now.temperature;
        for (ForecastSample hour : hours) {
            if (hour.timeMillis - now.timeMillis > DAY_MILLIS) {
                break;
            }
            max = Math.max(max, hour.temperature);
            min = Math.min(min, hour.temperature);
        }
        Boolean daySaid = now.symbol.isEmpty() ? null : MetSymbol.isDay(now.symbol);
        boolean day = daySaid != null ? daySaid : Sun.isUp(latitude, longitude, now.timeMillis);
        int code = now.symbol.isEmpty() ? 3 : MetSymbol.wmoCode(now.symbol);
        return new CurrentWeather(
                now.temperature,
                FeelsLike.apparent(now.temperature, now.humidity, now.windSpeed),
                (int) Math.round(now.humidity),
                now.windSpeed,
                code,
                day,
                max,
                min);
    }
}
