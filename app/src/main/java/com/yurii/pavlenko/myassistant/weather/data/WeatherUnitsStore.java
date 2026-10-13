package com.yurii.pavlenko.myassistant.weather.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.yurii.pavlenko.myassistant.weather.logic.TemperatureUnit;
import com.yurii.pavlenko.myassistant.weather.logic.WindUnit;

/** Remembers the chosen units. */
public final class WeatherUnitsStore {

    private static final String PREFS = "weather_units";
    private static final String KEY_TEMPERATURE = "temperature";
    private static final String KEY_WIND = "wind";

    private final SharedPreferences prefs;

    public WeatherUnitsStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public WeatherUnits load() {
        return new WeatherUnits(
                read(TemperatureUnit.class, KEY_TEMPERATURE, TemperatureUnit.CELSIUS),
                read(WindUnit.class, KEY_WIND, WindUnit.METRES_PER_SECOND));
    }

    public void save(WeatherUnits units) {
        prefs.edit()
                .putString(KEY_TEMPERATURE, units.getTemperature().name())
                .putString(KEY_WIND, units.getWind().name())
                .apply();
    }

    private <E extends Enum<E>> E read(Class<E> type, String key, E fallback) {
        String saved = prefs.getString(key, null);
        if (saved != null) {
            try {
                return Enum.valueOf(type, saved);
            } catch (IllegalArgumentException ignored) {
                // An unknown value falls back to the default
            }
        }
        return fallback;
    }
}
