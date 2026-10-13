package com.yurii.pavlenko.myassistant.currency.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.yurii.pavlenko.myassistant.currency.logic.ExchangeRates;

import java.util.HashMap;
import java.util.Map;

/** Keeps the latest rates on the phone so that the converter also works without internet. */
public final class RatesStore {

    private static final String PREFS = "currency_rates";
    private static final String KEY_BASE = "base";
    private static final String KEY_UPDATED = "updated";
    private static final String KEY_RATES = "rates";
    private static final String ENTRY_SEPARATOR = ";";
    private static final String VALUE_SEPARATOR = ":";

    private final SharedPreferences prefs;

    public RatesStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Returns null when nothing has been saved yet. */
    public ExchangeRates load() {
        String base = prefs.getString(KEY_BASE, null);
        String saved = prefs.getString(KEY_RATES, null);
        if (base == null || saved == null) {
            return null;
        }
        Map<String, Double> rates = new HashMap<>();
        for (String entry : saved.split(ENTRY_SEPARATOR)) {
            String[] parts = entry.split(VALUE_SEPARATOR);
            if (parts.length == 2) {
                try {
                    rates.put(parts[0], Double.parseDouble(parts[1]));
                } catch (NumberFormatException ignored) {
                    // A damaged entry is skipped; the rest of the rates stay usable
                }
            }
        }
        return rates.isEmpty() ? null : new ExchangeRates(base, rates, prefs.getLong(KEY_UPDATED, 0));
    }

    public void save(ExchangeRates rates) {
        StringBuilder text = new StringBuilder();
        for (Map.Entry<String, Double> entry : rates.toMap().entrySet()) {
            if (text.length() > 0) {
                text.append(ENTRY_SEPARATOR);
            }
            text.append(entry.getKey()).append(VALUE_SEPARATOR).append(entry.getValue());
        }
        prefs.edit()
                .putString(KEY_BASE, rates.getBase())
                .putLong(KEY_UPDATED, rates.getUpdatedAtMillis())
                .putString(KEY_RATES, text.toString())
                .apply();
    }
}
