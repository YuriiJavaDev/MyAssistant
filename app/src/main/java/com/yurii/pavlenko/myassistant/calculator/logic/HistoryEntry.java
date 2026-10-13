package com.yurii.pavlenko.myassistant.calculator.logic;

/** One finished calculation. */
public final class HistoryEntry {

    private final String expression;
    private final String result;

    public HistoryEntry(String expression, String result) {
        this.expression = expression;
        this.result = result;
    }

    public String getExpression() {
        return expression;
    }

    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        return expression + " = " + result;
    }
}
