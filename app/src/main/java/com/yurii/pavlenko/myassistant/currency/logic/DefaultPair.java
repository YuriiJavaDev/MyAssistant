package com.yurii.pavlenko.myassistant.currency.logic;

import java.util.List;

/**
 * The pair shown before the user chooses one: the money of the country the phone is in against the
 * US dollar, the world's reference currency.
 */
public final class DefaultPair {

    private static final String REFERENCE = "USD";
    private static final String REFERENCE_FOR_DOLLAR_COUNTRIES = "EUR";

    private DefaultPair() {
    }

    /** @param home currency of the country where the phone is, or null when it is not known */
    public static CurrencyPair choose(String home, ExchangeRates rates) {
        String from = home != null && rates.has(home) ? home : REFERENCE;
        String to = from.equals(REFERENCE) ? REFERENCE_FOR_DOLLAR_COUNTRIES : REFERENCE;
        if (!rates.has(to) || to.equals(from)) {
            to = anyOtherThan(from, rates.codes());
        }
        return new CurrencyPair(from, to);
    }

    private static String anyOtherThan(String code, List<String> codes) {
        for (String candidate : codes) {
            if (!candidate.equals(code)) {
                return candidate;
            }
        }
        return code;
    }
}
