package com.yurii.pavlenko.myassistant.currency.logic;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/** Turns amounts, rates and currencies into text for the screen. */
public final class MoneyFormat {

    private static final BigDecimal SMALL = new BigDecimal("0.01");
    private static final double LARGE_RATE = 100;
    private static final double UNIT_RATE = 1;
    private static final int MONEY_DIGITS = 2;
    private static final int SMALL_MONEY_DIGITS = 6;

    private MoneyFormat() {
    }

    /** An amount of money: two decimals, or more when it is too small to show otherwise. */
    public static String amount(BigDecimal value, Locale locale) {
        NumberFormat format = NumberFormat.getNumberInstance(locale);
        boolean tiny = value.signum() != 0 && value.abs().compareTo(SMALL) < 0;
        format.setMinimumFractionDigits(tiny ? 0 : MONEY_DIGITS);
        format.setMaximumFractionDigits(tiny ? SMALL_MONEY_DIGITS : MONEY_DIGITS);
        return format.format(value);
    }

    /** An exchange rate with enough digits to be useful whether it is 0.0001 or 1500. */
    public static String rate(double value, Locale locale) {
        NumberFormat format = NumberFormat.getNumberInstance(locale);
        format.setMaximumFractionDigits(value >= LARGE_RATE ? 2 : value >= UNIT_RATE ? 4 : SMALL_MONEY_DIGITS);
        return format.format(value);
    }

    /** The code with the English name of the currency, such as "ILS · Israeli New Shekel". */
    public static String label(String code) {
        try {
            return code + " · " + Currency.getInstance(code).getDisplayName(Locale.ENGLISH);
        } catch (IllegalArgumentException unknown) {
            return code;
        }
    }
}
