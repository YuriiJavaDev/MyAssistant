package com.yurii.pavlenko.myassistant.currency.data;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.util.Currency;
import java.util.Locale;

/**
 * Finds the currency of the country the phone is in. The mobile network country follows the phone
 * when travelling and needs no permission; the SIM country and the phone's region are fallbacks.
 */
public final class HomeCurrency {

    private HomeCurrency() {
    }

    /** Returns null when the country or its currency cannot be told. */
    public static String find(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String[] countries = {
                telephony == null ? "" : telephony.getNetworkCountryIso(),
                telephony == null ? "" : telephony.getSimCountryIso(),
                Locale.getDefault().getCountry()
        };
        for (String country : countries) {
            String code = currencyOf(country);
            if (code != null) {
                return code;
            }
        }
        return null;
    }

    private static String currencyOf(String country) {
        if (country == null || country.length() != 2) {
            return null;
        }
        try {
            return Currency.getInstance(new Locale("", country.toUpperCase(Locale.ROOT))).getCurrencyCode();
        } catch (IllegalArgumentException unknownCountry) {
            return null;
        }
    }
}
