package com.yurii.pavlenko.myassistant.weather.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Free, key-less web services: Open-Meteo for city search and weather, BigDataCloud to name the place
 * where the phone is. All calls block, so they must run off the main thread.
 */
public final class WeatherService {

    private static final String SEARCH_URL = "https://geocoding-api.open-meteo.com/v1/search?count=%d&language=en&format=json&name=%s";
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s"
            + "&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,is_day"
            + "&daily=temperature_2m_max,temperature_2m_min&timezone=auto&forecast_days=1&wind_speed_unit=ms";
    private static final String REVERSE_URL = "https://api.bigdatacloud.net/data/reverse-geocode-client"
            + "?latitude=%s&longitude=%s&localityLanguage=en";
    private static final int MAX_MATCHES = 5;
    private static final String UNKNOWN_PLACE = "Current location";

    private WeatherService() {
    }

    /** Finds cities by a name written in any language, best matches first. */
    public static List<Place> searchCity(String query) throws IOException {
        String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.name());
        JSONObject response = HttpJson.get(String.format(Locale.US, SEARCH_URL, MAX_MATCHES, encoded));
        List<Place> places = new ArrayList<>();
        JSONArray results = response.optJSONArray("results");
        for (int i = 0; results != null && i < results.length(); i++) {
            JSONObject item = results.optJSONObject(i);
            if (item != null) {
                places.add(new Place(item.optString("name"), item.optString("admin1"), item.optString("country"),
                        item.optDouble("latitude"), item.optDouble("longitude")));
            }
        }
        return places;
    }

    /** Names the place at the given coordinates; falls back to a neutral name when the lookup fails. */
    public static Place describeLocation(double latitude, double longitude) {
        try {
            JSONObject response = HttpJson.get(String.format(Locale.US, REVERSE_URL, latitude, longitude));
            String name = firstNonEmpty(response.optString("city"), response.optString("locality"));
            if (!name.isEmpty()) {
                return new Place(name, response.optString("principalSubdivision"),
                        response.optString("countryName"), latitude, longitude);
            }
        } catch (IOException ignored) {
            // The weather itself is still available for these coordinates
        }
        return new Place(UNKNOWN_PLACE, "", "", latitude, longitude);
    }

    public static CurrentWeather loadWeather(Place place) throws IOException {
        JSONObject response = HttpJson.get(String.format(Locale.US, FORECAST_URL, place.getLatitude(), place.getLongitude()));
        try {
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

    private static String firstNonEmpty(String first, String second) {
        return first.isEmpty() ? second : first;
    }
}
