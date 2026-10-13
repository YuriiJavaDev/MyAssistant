package com.yurii.pavlenko.myassistant.calculator.logic;

/** The unit in which trigonometric functions take and return angles. */
public enum AngleMode {
    DEGREES, RADIANS;

    public AngleMode other() {
        return this == DEGREES ? RADIANS : DEGREES;
    }
}
