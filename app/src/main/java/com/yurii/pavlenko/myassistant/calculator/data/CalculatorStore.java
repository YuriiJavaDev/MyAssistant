package com.yurii.pavlenko.myassistant.calculator.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.yurii.pavlenko.myassistant.calculator.logic.AngleMode;

/** Remembers the memory value and the angle unit between launches. */
public final class CalculatorStore {

    private static final String PREFS = "calculator";
    private static final String KEY_MEMORY = "memory";
    private static final String KEY_ANGLE = "angle";

    private final SharedPreferences prefs;

    public CalculatorStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public double loadMemory() {
        try {
            return Double.parseDouble(prefs.getString(KEY_MEMORY, "0"));
        } catch (NumberFormatException damaged) {
            return 0;
        }
    }

    public AngleMode loadAngleMode() {
        try {
            return AngleMode.valueOf(prefs.getString(KEY_ANGLE, AngleMode.DEGREES.name()));
        } catch (IllegalArgumentException unknown) {
            return AngleMode.DEGREES;
        }
    }

    public void save(double memory, AngleMode angleMode) {
        prefs.edit()
                .putString(KEY_MEMORY, Double.toString(memory))
                .putString(KEY_ANGLE, angleMode.name())
                .apply();
    }
}
