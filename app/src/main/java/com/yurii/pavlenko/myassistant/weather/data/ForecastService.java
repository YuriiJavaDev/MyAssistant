package com.yurii.pavlenko.myassistant.weather.data;

import com.yurii.pavlenko.myassistant.network.HttpText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Loads the current weather from the free open data of MET Norway (the Norwegian Meteorological Institute,
 * licence CC BY 4.0). Blocking: call it off the main thread.
 *
 * <p>Their terms ask for an identifying User-Agent, coordinates with at most four decimals, and that data is not
 * requested again before the time the server says it expires; answers are therefore kept until then.
 */
public final class ForecastService {

    private static final String FORECAST_URL = "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=%s&lon=%s";
    /** The service wants at most four decimals, which is about ten metres. */
    private static final String COORDINATE_FORMAT = "%.4f";
    private static final int HOURS_NEEDED = 26;
    private static final long DEFAULT_KEEP_MILLIS = 10 * 60 * 1000L;
    private static final int CACHE_SIZE = 20;

    private static final Map<String, Cached> CACHE = new LinkedHashMap<String, Cached>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Cached> eldest) {
            return size() > CACHE_SIZE;
        }
    };

    private ForecastService() {
    }

    private static final class Cached {
        final CurrentWeather weather;
        final long validUntilMillis;

        Cached(CurrentWeather weather, long validUntilMillis) {
            this.weather = weather;
            this.validUntilMillis = validUntilMillis;
        }
    }

    public static CurrentWeather load(Place place) throws IOException {
        String latitude = String.format(Locale.US, COORDINATE_FORMAT, place.getLatitude());
        String longitude = String.format(Locale.US, COORDINATE_FORMAT, place.getLongitude());
        String key = latitude + "," + longitude;
        long now = System.currentTimeMillis();
        synchronized (CACHE) {
            Cached cached = CACHE.get(key);
            if (cached != null && now < cached.validUntilMillis) {
                return cached.weather;
            }
        }
        HttpText.Response response = HttpText.getResponse(String.format(Locale.US, FORECAST_URL, latitude, longitude));
        CurrentWeather weather = ForecastSummary.of(hoursOf(response.getBody()), place.getLatitude(), place.getLongitude());
        long expires = response.getExpiresAtMillis();
        long validUntil = expires > now ? expires : now + DEFAULT_KEEP_MILLIS;
        synchronized (CACHE) {
            CACHE.put(key, new Cached(weather, validUntil));
        }
        return weather;
    }

    private static List<ForecastSample> hoursOf(String body) throws IOException {
        try {
            JSONArray series = new JSONObject(body).getJSONObject("properties").getJSONArray("timeseries");
            List<ForecastSample> hours = new ArrayList<>();
            for (int i = 0; i < series.length() && hours.size() < HOURS_NEEDED; i++) {
                JSONObject entry = series.getJSONObject(i);
                JSONObject data = entry.getJSONObject("data");
                JSONObject details = data.getJSONObject("instant").getJSONObject("details");
                hours.add(new ForecastSample(
                        Instant.parse(entry.getString("time")).toEpochMilli(),
                        details.getDouble("air_temperature"),
                        details.optDouble("relative_humidity", 0),
                        details.optDouble("wind_speed", 0),
                        symbolOf(data)));
            }
            if (hours.isEmpty()) {
                throw new IOException("The forecast has no hours");
            }
            return hours;
        } catch (JSONException e) {
            throw new IOException("Unexpected weather data", e);
        }
    }

    /** The symbol for the next hour, or for a longer span when the service gives no hourly one; empty when there is none. */
    private static String symbolOf(JSONObject data) {
        for (String span : new String[]{"next_1_hours", "next_6_hours", "next_12_hours"}) {
            JSONObject next = data.optJSONObject(span);
            JSONObject summary = next == null ? null : next.optJSONObject("summary");
            if (summary != null && !summary.optString("symbol_code").isEmpty()) {
                return summary.optString("symbol_code");
            }
        }
        return "";
    }
}
