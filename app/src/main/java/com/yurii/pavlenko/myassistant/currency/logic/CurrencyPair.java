package com.yurii.pavlenko.myassistant.currency.logic;

/** The currency being converted from and the one it is converted to. */
public final class CurrencyPair {

    private final String from;
    private final String to;

    public CurrencyPair(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public CurrencyPair swapped() {
        return new CurrencyPair(to, from);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CurrencyPair
                && from.equals(((CurrencyPair) other).from) && to.equals(((CurrencyPair) other).to);
    }

    @Override
    public int hashCode() {
        return 31 * from.hashCode() + to.hashCode();
    }
}
