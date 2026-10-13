package com.yurii.pavlenko.myassistant.currency.logic;

import java.math.BigDecimal;

/** The result of converting an amount between the two currencies of a pair. */
public final class Conversion {

    private final BigDecimal amount;
    private final BigDecimal result;
    private final double rate;

    private Conversion(BigDecimal amount, BigDecimal result, double rate) {
        this.amount = amount;
        this.result = result;
        this.rate = rate;
    }

    /** Returns null when the typed text is not an amount. */
    public static Conversion of(ExchangeRates rates, CurrencyPair pair, String amountText) {
        BigDecimal amount = AmountParser.parse(amountText);
        if (amount == null) {
            return null;
        }
        return new Conversion(amount, rates.convert(amount, pair.getFrom(), pair.getTo()),
                rates.rate(pair.getFrom(), pair.getTo()));
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getResult() {
        return result;
    }

    public double getRate() {
        return rate;
    }
}
