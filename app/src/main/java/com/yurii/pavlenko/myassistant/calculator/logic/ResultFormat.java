package com.yurii.pavlenko.myassistant.calculator.logic;

import java.math.BigDecimal;
import java.math.MathContext;

/** Shows a result with twelve significant digits, which hides binary rounding noise such as 0.1 + 0.2. */
public final class ResultFormat {

    private static final MathContext DIGITS = new MathContext(12);
    private static final double PLAIN_MIN = 1e-9;
    private static final double PLAIN_MAX = 1e15;

    private ResultFormat() {
    }

    public static String format(double value) {
        if (value == 0) {
            return "0";
        }
        BigDecimal rounded = new BigDecimal(value).round(DIGITS).stripTrailingZeros();
        double size = Math.abs(value);
        return size >= PLAIN_MIN && size < PLAIN_MAX ? rounded.toPlainString() : rounded.toString();
    }
}
