package com.yurii.pavlenko.myassistant.currency.viewmodel;

import com.yurii.pavlenko.myassistant.currency.logic.ExchangeRates;

/** What the converter has to show about the rates: loading, ready (maybe old), or unavailable. */
public final class RatesState {

    public enum Status { LOADING, READY, ERROR }

    private final Status status;
    private final ExchangeRates rates;
    private final boolean outdated;

    private RatesState(Status status, ExchangeRates rates, boolean outdated) {
        this.status = status;
        this.rates = rates;
        this.outdated = outdated;
    }

    public static RatesState loading() {
        return new RatesState(Status.LOADING, null, false);
    }

    /** @param outdated true when the latest download failed and saved rates are shown instead */
    public static RatesState ready(ExchangeRates rates, boolean outdated) {
        return new RatesState(Status.READY, rates, outdated);
    }

    public static RatesState error() {
        return new RatesState(Status.ERROR, null, false);
    }

    public Status getStatus() {
        return status;
    }

    public ExchangeRates getRates() {
        return rates;
    }

    public boolean isOutdated() {
        return outdated;
    }
}
