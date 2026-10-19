package com.yurii.pavlenko.myassistant.flights.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.yurii.pavlenko.myassistant.flights.logic.ReportQuota;

/** Remembers how many flight reports the journal may hold. */
public final class ReportLimitStore {

    private static final String PREFS = "flight_report_limit";
    private static final String KEY_LIMIT = "limit";

    private final SharedPreferences prefs;

    public ReportLimitStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public int load() {
        return ReportQuota.sanitize(prefs.getInt(KEY_LIMIT, ReportQuota.DEFAULT_LIMIT));
    }

    public void save(int limit) {
        prefs.edit().putInt(KEY_LIMIT, ReportQuota.sanitize(limit)).apply();
    }
}
