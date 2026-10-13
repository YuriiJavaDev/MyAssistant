package com.yurii.pavlenko.myassistant.weather.data;

import com.yurii.pavlenko.myassistant.network.HttpText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

/** Loads the current weather from the free Open-Meteo service. Blocking: call it off the main thread. */
public final class ForecastService {

    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s"
            + "&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,is_day"
            + "&daily=temperature_2m_max,temperature_2m_min&timezone=auto&forecast_days=1&wind_speed_unit=ms";

    private ForecastService() {
    }

    public static CurrentWeather load(Place place) throws IOException {
        String url = String.format(Locale.US, FORECAST_URL, place.getLatitude(), place.getLongitude());
        try {
            JSONObject response = new JSONObject(HttpText.get(url));
            JSONObject current = response.getJSONObject("current");
            JSONObject daily = response.getJSONObject("daily");
            return new CurrentWeather(
                    current.getDouble("temperature_2m"),
                    current.getDouble("apparent_temperature"),
                    current.getInt("relative_humidity_2m"),
                    current.getDouble("wind_speed_10m"),
                    current.getInt("weather_code"),
                    current.getInt("is_day") == 1,
                    daily.getJSONArray("temperature_2m_max").getDouble(0),
                    daily.getJSONArray("temperature_2m_min").getDouble(0));
        } catch (JSONException e) {
            throw new IOException("Unexpected weather data", e);
        }
    }
}
