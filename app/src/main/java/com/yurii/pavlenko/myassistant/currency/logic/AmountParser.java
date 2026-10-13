package com.yurii.pavlenko.myassistant.currency.logic;

import java.math.BigDecimal;

/** Reads an amount typed by the user, whatever the decimal separator and digit grouping they use. */
public final class AmountParser {

    private static final int MAX_DIGITS = 15;

    private AmountParser() {
    }

    /** Returns null when the text is not a usable non-negative number. */
    public static BigDecimal parse(String text) {
        String cleaned = text.replaceAll("[\\s\\u00A0\\u202F']", "");
        if (cleaned.isEmpty() || cleaned.replaceAll("\\D", "").length() > MAX_DIGITS) {
            return null;
        }
        int lastComma = cleaned.lastIndexOf(',');
        int lastDot = cleaned.lastIndexOf('.');
        int decimalAt = Math.max(lastComma, lastDot);
        if (lastComma >= 0 && lastDot >= 0) {
            String integer = cleaned.substring(0, decimalAt).replaceAll("[.,]", "");
            return build(integer, cleaned.substring(decimalAt + 1));
        }
        boolean repeated = decimalAt >= 0 && cleaned.indexOf(cleaned.charAt(decimalAt)) != decimalAt;
        if (repeated) {
            return build(cleaned.replaceAll("[.,]", ""), "");
        }
        return decimalAt < 0 ? build(cleaned, "") : build(cleaned.substring(0, decimalAt), cleaned.substring(decimalAt + 1));
    }

    private static BigDecimal build(String integer, String fraction) {
        if (!integer.matches("\\d*") || !fraction.matches("\\d*") || (integer.isEmpty() && fraction.isEmpty())) {
            return null;
        }
        return new BigDecimal((integer.isEmpty() ? "0" : integer) + (fraction.isEmpty() ? "" : "." + fraction));
    }
}
