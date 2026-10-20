package com.yurii.pavlenko.myassistant.calculator.logic;

/** A mistake in the expression or a result that cannot be shown. */
public final class CalculationException extends Exception {

    private final CalcError error;

    public CalculationException(CalcError error) {
        super(error.name());
        this.error = error;
    }

    public CalcError getError() {
        return error;
    }
}
