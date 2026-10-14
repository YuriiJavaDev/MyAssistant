package com.yurii.pavlenko.myassistant.calculator.logic;

/** A mistake in the expression or a result that cannot be shown; the message is meant for the user. */
public final class CalculationException extends Exception {

    public CalculationException(String message) {
        super(message);
    }
}
