package com.yurii.pavlenko.myassistant.currency.logic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Exchange rates of many currencies against one base currency, as of a moment in time. */
public final class ExchangeRates {

    private final String base;
    private final Map<String, Double> perBase;
    private final long updatedAtMillis;

    /** @param perBase how much of each currency one unit of the base currency buys */
    public ExchangeRates(String base, Map<String, Double> perBase, long updatedAtMillis) {
        this.base = base;
        this.perBase = new TreeMap<>(perBase);
        this.perBase.put(base, 1.0);
        this.updatedAtMillis = updatedAtMillis;
    }

    public String getBase() {
        return base;
    }

    public long getUpdatedAtMillis() {
        return updatedAtMillis;
    }

    public boolean has(String code) {
        return perBase.containsKey(code);
    }

    /** All currency codes in alphabetical order. */
    public List<String> codes() {
        return Collections.unmodifiableList(new ArrayList<>(perBase.keySet()));
    }

    /** How much of {@code to} one unit of {@code from} buys. */
    public double rate(String from, String to) {
        return perBase.get(to) / perBase.get(from);
    }

    public BigDecimal convert(BigDecimal amount, String from, String to) {
        return amount.multiply(BigDecimal.valueOf(rate(from, to)));
    }

    /** The rates by currency code, for saving. */
    public Map<String, Double> toMap() {
        return Collections.unmodifiableMap(perBase);
    }
}
