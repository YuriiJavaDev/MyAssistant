package com.yurii.pavlenko.myassistant.calculator.logic;

/** What the display shows; null fields mean there is nothing to show. */
public final class CalculatorDisplay {

    private final String expression;
    private final String preview;
    private final String error;
    private final HistoryEntry last;
    private final String memory;
    private final AngleMode angleMode;

    CalculatorDisplay(String expression, String preview, String error, HistoryEntry last,
                      String memory, AngleMode angleMode) {
        this.expression = expression;
        this.preview = preview;
        this.error = error;
        this.last = last;
        this.memory = memory;
        this.angleMode = angleMode;
    }

    public String getExpression() {
        return expression;
    }

    /** The result of the expression typed so far, shown while the user is still typing. */
    public String getPreview() {
        return preview;
    }

    public String getError() {
        return error;
    }

    /** The latest finished calculation. */
    public HistoryEntry getLast() {
        return last;
    }

    /** The value kept in memory, or null when the memory is empty. */
    public String getMemory() {
        return memory;
    }

    public AngleMode getAngleMode() {
        return angleMode;
    }
}
