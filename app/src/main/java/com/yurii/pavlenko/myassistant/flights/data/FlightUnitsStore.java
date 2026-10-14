package com.yurii.pavlenko.myassistant.flights.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.yurii.pavlenko.myassistant.flights.logic.AltitudeUnit;
import com.yurii.pavlenko.myassistant.flights.logic.ClimbUnit;
import com.yurii.pavlenko.myassistant.flights.logic.SpeedUnit;

/** Remembers the chosen units. */
public final class FlightUnitsStore {

    private static final String PREFS = "flight_units";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_CLIMB = "climb";

    private final SharedPreferences prefs;

    public FlightUnitsStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public FlightUnits load() {
        return new FlightUnits(
                read(SpeedUnit.class, KEY_SPEED, SpeedUnit.KILOMETRES_PER_HOUR),
                read(AltitudeUnit.class, KEY_ALTITUDE, AltitudeUnit.METRES),
                read(ClimbUnit.class, KEY_CLIMB, ClimbUnit.METRES_PER_SECOND));
    }

    public void save(FlightUnits units) {
        prefs.edit()
                .putString(KEY_SPEED, units.getSpeed().name())
                .putString(KEY_ALTITUDE, units.getAltitude().name())
                .putString(KEY_CLIMB, units.getClimb().name())
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
