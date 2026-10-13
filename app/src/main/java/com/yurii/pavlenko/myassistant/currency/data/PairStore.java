package com.yurii.pavlenko.myassistant.currency.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.yurii.pavlenko.myassistant.currency.logic.CurrencyPair;

/** Remembers the pair of currencies the user chose and the amount they typed. */
public final class PairStore {

    private static final String PREFS = "currency_selection";
    private static final String KEY_FROM = "from";
    private static final String KEY_TO = "to";
    private static final String KEY_AMOUNT = "amount";
    private static final String DEFAULT_AMOUNT = "100";

    private final SharedPreferences prefs;

    public PairStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Returns null until the user has chosen a pair. */
    public CurrencyPair loadPair() {
        String from = prefs.getString(KEY_FROM, null);
        String to = prefs.getString(KEY_TO, null);
        return from == null || to == null ? null : new CurrencyPair(from, to);
    }

    public void savePair(CurrencyPair pair) {
        prefs.edit().putString(KEY_FROM, pair.getFrom()).putString(KEY_TO, pair.getTo()).apply();
    }

    public String loadAmount() {
        return prefs.getString(KEY_AMOUNT, DEFAULT_AMOUNT);
    }

    public void saveAmount(String amount) {
        prefs.edit().putString(KEY_AMOUNT, amount).apply();
    }
}
