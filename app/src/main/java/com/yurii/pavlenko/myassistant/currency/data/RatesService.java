package com.yurii.pavlenko.myassistant.currency.data;

import com.yurii.pavlenko.myassistant.currency.logic.ExchangeRates;
import com.yurii.pavlenko.myassistant.network.HttpText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Loads daily exchange rates for about 160 currencies from the free open endpoint of ExchangeRate-API. */
public final class RatesService {

    private static final String BASE = "USD";
    private static final String URL = "https://open.er-api.com/v6/latest/" + BASE;

    private RatesService() {
    }

    /** Blocking: call it off the main thread. */
    public static ExchangeRates load() throws IOException {
        try {
            JSONObject response = new JSONObject(HttpText.get(URL));
            if (!"success".equals(response.optString("result"))) {
                throw new IOException("The rates service reported an error");
            }
            JSONObject rates = response.getJSONObject("rates");
            Map<String, Double> perDollar = new HashMap<>();
            Iterator<String> codes = rates.keys();
            while (codes.hasNext()) {
                String code = codes.next();
                double rate = rates.optDouble(code);
                if (rate > 0) {
                    perDollar.put(code, rate);
                }
            }
            long updatedAt = TimeUnit.SECONDS.toMillis(response.optLong("time_last_update_unix"));
            return new ExchangeRates(BASE, perDollar, updatedAt);
        } catch (JSONException e) {
            throw new IOException("Unexpected answer from the rates service", e);
        }
    }
}
