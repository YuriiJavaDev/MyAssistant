package com.yurii.pavlenko.myassistant.calculator.logic;

/** The named functions of the calculator and the factorial. */
final class MathFunctions {

    private static final String INVALID = "Invalid input";
    private static final double TRIG_ZERO = 1e-14;
    private static final int MAX_FACTORIAL = 170;

    private MathFunctions() {
    }

    static boolean isFunction(String name) {
        switch (name) {
            case "sin": case "cos": case "tan": case "asin": case "acos": case "atan":
            case "ln": case "log": case "exp": case "sqrt": case "cbrt": case "abs":
                return true;
            default:
                return false;
        }
    }

    static double apply(String name, double x, AngleMode mode) throws CalculationException {
        switch (name) {
            case "sin": return trig(Math.sin(angle(x, mode)));
            case "cos": return trig(Math.cos(angle(x, mode)));
            case "tan": return tan(x, mode);
            case "asin": return inverseTrig(Math.asin(requireWithin(x)), mode);
            case "acos": return inverseTrig(Math.acos(requireWithin(x)), mode);
            case "atan": return inverseTrig(Math.atan(x), mode);
            case "ln": return Math.log(requirePositive(x));
            case "log": return Math.log10(requirePositive(x));
            case "exp": return Math.exp(x);
            case "sqrt": return Math.sqrt(requireNotNegative(x));
            case "cbrt": return Math.cbrt(x);
            case "abs": return Math.abs(x);
            default: throw new CalculationException(INVALID);
        }
    }

    static double factorial(double x) throws CalculationException {
        if (x < 0 || x != Math.floor(x)) {
            throw new CalculationException(INVALID);
        }
        if (x > MAX_FACTORIAL) {
            throw new CalculationException("Result is too large");
        }
        double result = 1;
        for (int i = 2; i <= x; i++) {
            result *= i;
        }
        return result;
    }

    private static double angle(double x, AngleMode mode) {
        return mode == AngleMode.DEGREES ? Math.toRadians(x) : x;
    }

    private static double inverseTrig(double radians, AngleMode mode) {
        return mode == AngleMode.DEGREES ? Math.toDegrees(radians) : radians;
    }

    private static double tan(double x, AngleMode mode) throws CalculationException {
        boolean vertical = mode == AngleMode.DEGREES
                ? Math.abs(x % 180) == 90
                : Math.abs(Math.cos(x)) < TRIG_ZERO;
        if (vertical) {
            throw new CalculationException("Undefined");
        }
        return trig(Math.tan(angle(x, mode)));
    }

    /** Removes the rounding noise that makes cos(90°) come out as 6e-17 instead of 0. */
    private static double trig(double value) {
        return Math.abs(value) < TRIG_ZERO ? 0 : value;
    }

    private static double requireWithin(double x) throws CalculationException {
        if (x < -1 || x > 1) {
            throw new CalculationException(INVALID);
        }
        return x;
    }

    private static double requirePositive(double x) throws CalculationException {
        if (x <= 0) {
            throw new CalculationException(INVALID);
        }
        return x;
    }

    private static double requireNotNegative(double x) throws CalculationException {
        if (x < 0) {
            throw new CalculationException(INVALID);
        }
        return x;
    }
}
