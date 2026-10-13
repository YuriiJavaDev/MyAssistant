package com.yurii.pavlenko.myassistant.calculator.logic;

/**
 * Calculates a typed expression such as {@code 2(3+4)^2 - sin(30)}. Powers bind tighter than a
 * minus sign, so {@code -2^2} is -4; unclosed parentheses are closed at the end; two operands
 * written side by side are multiplied.
 */
public final class ExpressionEvaluator {

    private static final int MAX_LENGTH = 300;
    private static final int MAX_DEPTH = 100;
    private static final String INVALID_EXPRESSION = "Invalid expression";
    private static final String DIVISION_BY_ZERO = "Division by zero";

    private final String text;
    private final AngleMode mode;
    private final double answer;
    private int position;
    private int depth;

    private ExpressionEvaluator(String expression, AngleMode mode, double answer) {
        this.text = expression.replace('−', '-').replace('×', '*').replace('÷', '/')
                .replaceAll("\\s+", "");
        this.mode = mode;
        this.answer = answer;
    }

    /** @param answer the value that "Ans" stands for */
    public static double evaluate(String expression, AngleMode mode, double answer) throws CalculationException {
        if (expression.length() > MAX_LENGTH) {
            throw new CalculationException("Expression is too long");
        }
        ExpressionEvaluator evaluator = new ExpressionEvaluator(expression, mode, answer);
        double value = evaluator.parseExpression();
        if (!evaluator.atEnd()) {
            throw new CalculationException(INVALID_EXPRESSION);
        }
        if (Double.isNaN(value)) {
            throw new CalculationException("Invalid input");
        }
        if (Double.isInfinite(value)) {
            throw new CalculationException("Result is too large");
        }
        return value;
    }

    private double parseExpression() throws CalculationException {
        double value = parseTerm();
        while (true) {
            if (accept('+')) {
                value += parseTerm();
            } else if (accept('-')) {
                value -= parseTerm();
            } else {
                return value;
            }
        }
    }

    private double parseTerm() throws CalculationException {
        double value = parseUnary();
        while (true) {
            if (accept('*')) {
                value *= parseUnary();
            } else if (accept('/')) {
                value /= nonZero(parseUnary());
            } else if (acceptWord("mod")) {
                double divisor = nonZero(parseUnary());
                value -= divisor * Math.floor(value / divisor);
            } else if (startsOperand()) {
                value *= parseUnary();
            } else {
                return value;
            }
        }
    }

    private double parseUnary() throws CalculationException {
        enter();
        try {
            if (accept('-')) {
                return -parseUnary();
            }
            if (accept('+')) {
                return parseUnary();
            }
            return parsePower();
        } finally {
            depth--;
        }
    }

    private double parsePower() throws CalculationException {
        double base = parsePostfix();
        if (accept('^')) {
            return Math.pow(base, parseUnary());
        }
        return base;
    }

    private double parsePostfix() throws CalculationException {
        double value = parsePrimary();
        while (true) {
            if (accept('!')) {
                value = MathFunctions.factorial(value);
            } else if (accept('%')) {
                value /= 100;
            } else if (accept('²')) {
                value *= value;
            } else if (accept('³')) {
                value = value * value * value;
            } else {
                return value;
            }
        }
    }

    private double parsePrimary() throws CalculationException {
        if (accept('(')) {
            return parseParenthesized();
        }
        if (accept('π')) {
            return Math.PI;
        }
        if (peekIsDigit() || peek() == '.') {
            return parseNumber();
        }
        if (peekIsLetter()) {
            return parseName();
        }
        throw new CalculationException(INVALID_EXPRESSION);
    }

    private double parseParenthesized() throws CalculationException {
        enter();
        try {
            double value = parseExpression();
            if (!atEnd()) {
                expect(')');
            }
            return value;
        } finally {
            depth--;
        }
    }

    private double parseName() throws CalculationException {
        int start = position;
        while (peekIsLetter()) {
            position++;
        }
        String name = text.substring(start, position);
        if (MathFunctions.isFunction(name)) {
            expect('(');
            return MathFunctions.apply(name, parseParenthesized(), mode);
        }
        switch (name) {
            case "e": return Math.E;
            case "Ans": return answer;
            default: throw new CalculationException(INVALID_EXPRESSION);
        }
    }

    private double parseNumber() throws CalculationException {
        int start = position;
        boolean dot = false;
        while (peekIsDigit() || (peek() == '.' && !dot)) {
            dot |= peek() == '.';
            position++;
        }
        if (peek() == 'E' && startsExponent()) {
            position++;
            if (peek() == '+' || peek() == '-') {
                position++;
            }
            while (peekIsDigit()) {
                position++;
            }
        }
        String number = text.substring(start, position);
        if (number.equals(".")) {
            throw new CalculationException(INVALID_EXPRESSION);
        }
        return Double.parseDouble(number);
    }

    /** Results such as 1E+20 are shown with an exponent, and must be readable when typed on. */
    private boolean startsExponent() {
        int next = position + 1;
        if (next < text.length() && (text.charAt(next) == '+' || text.charAt(next) == '-')) {
            next++;
        }
        return next < text.length() && Character.isDigit(text.charAt(next));
    }

    private boolean startsOperand() {
        char next = peek();
        boolean modWord = text.startsWith("mod", position);
        return next == '(' || next == 'π' || next == '.' || Character.isDigit(next)
                || (Character.isLetter(next) && !modWord);
    }

    private double nonZero(double divisor) throws CalculationException {
        if (divisor == 0) {
            throw new CalculationException(DIVISION_BY_ZERO);
        }
        return divisor;
    }

    private void enter() throws CalculationException {
        if (++depth > MAX_DEPTH) {
            throw new CalculationException("Expression is too complex");
        }
    }

    private boolean atEnd() {
        return position >= text.length();
    }

    private char peek() {
        return atEnd() ? '\0' : text.charAt(position);
    }

    private boolean peekIsDigit() {
        return peek() >= '0' && peek() <= '9';
    }

    private boolean peekIsLetter() {
        return (peek() >= 'a' && peek() <= 'z') || (peek() >= 'A' && peek() <= 'Z');
    }

    private boolean accept(char symbol) {
        if (peek() == symbol) {
            position++;
            return true;
        }
        return false;
    }

    private boolean acceptWord(String word) {
        if (text.startsWith(word, position)) {
            position += word.length();
            return true;
        }
        return false;
    }

    private void expect(char symbol) throws CalculationException {
        if (!accept(symbol)) {
            throw new CalculationException(INVALID_EXPRESSION);
        }
    }
}
