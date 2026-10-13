package com.yurii.pavlenko.myassistant.calculator.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;

/** The calculator itself: it turns key presses into an expression, results, memory and history. */
public final class CalculatorEngine {

    private static final int MAX_LENGTH = 200;
    private static final int MAX_HISTORY = 50;
    private static final String OPERATORS = "+-×÷^";
    private static final String NUMBER_ENDINGS = "0123456789.)²³%!πe";

    private final DoubleSupplier random;
    private final List<HistoryEntry> history = new ArrayList<>();
    private AngleMode angleMode;
    private double memory;
    private double answer;
    private String expression = "";
    private String error;
    private boolean showingResult;

    /** @param random supplies numbers from 0 up to but not including 1 for the Rand key */
    public CalculatorEngine(AngleMode angleMode, double memory, DoubleSupplier random) {
        this.angleMode = angleMode;
        this.memory = memory;
        this.random = random;
    }

    public double getMemory() {
        return memory;
    }

    public AngleMode getAngleMode() {
        return angleMode;
    }

    /** Finished calculations, newest first. */
    public List<HistoryEntry> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void toggleAngleMode() {
        angleMode = angleMode.other();
    }

    /** Puts an expression from the history back into the display to be edited. */
    public void restore(String earlier) {
        expression = earlier;
        error = null;
        showingResult = false;
    }

    public void press(Key key) {
        error = null;
        switch (key.kind()) {
            case DIGIT: typeAfterFreshStart(key.text()); break;
            case DOT: typeDot(); break;
            case OPERATOR: typeOperator(key); break;
            case POSTFIX: typePostfix(key.text()); break;
            case PREFIX: typeAfterFreshStart(key.text()); break;
            case CLOSE: typeClose(); break;
            case RANDOM: typeValue(ResultFormat.format(random.getAsDouble())); break;
            case CLEAR: clear(); break;
            case BACK: back(); break;
            case EQUALS: equal(); break;
            case MEMORY_ADD: addToMemory(); break;
            case MEMORY_RECALL: recallMemory(); break;
            case MEMORY_CLEAR: memory = 0; break;
            default: break;
        }
    }

    public CalculatorDisplay display() {
        HistoryEntry last = history.isEmpty() ? null : history.get(0);
        String remembered = memory == 0 ? null : ResultFormat.format(memory);
        return new CalculatorDisplay(expression, preview(), error, last, remembered, angleMode);
    }

    private String preview() {
        if (showingResult || expression.isEmpty() || expression.matches("-?[0-9.]+")) {
            return null;
        }
        try {
            return ResultFormat.format(ExpressionEvaluator.evaluate(expression, angleMode, answer));
        } catch (CalculationException unfinished) {
            return null;
        }
    }

    private void typeAfterFreshStart(String text) {
        if (showingResult) {
            expression = "";
            showingResult = false;
        }
        append(text);
    }

    private void append(String text) {
        if (expression.length() + text.length() <= MAX_LENGTH) {
            expression += text;
        }
    }

    private void typeDot() {
        typeAfterFreshStart("");
        if (currentNumber().contains(".")) {
            return;
        }
        append(currentNumber().isEmpty() ? "0." : ".");
    }

    private String currentNumber() {
        int start = expression.length();
        while (start > 0 && "0123456789.".indexOf(expression.charAt(start - 1)) >= 0) {
            start--;
        }
        return expression.substring(start);
    }

    private void typeOperator(Key key) {
        showingResult = false;
        String text = key.text();
        if (expression.isEmpty()) {
            if (key == Key.SUBTRACT) {
                append(text);
            }
            return;
        }
        if (expression.endsWith("mod")) {
            expression = expression.substring(0, expression.length() - 3);
        } else if (endsWithOperator()) {
            boolean negativeSign = key == Key.SUBTRACT && !expression.endsWith("-") && !expression.endsWith("+");
            if (!negativeSign) {
                expression = expression.substring(0, expression.length() - 1);
            }
        } else if (expression.endsWith("(") && key != Key.SUBTRACT) {
            return;
        }
        append(text);
    }

    private void typePostfix(String text) {
        showingResult = false;
        if (endsWithNumberPart() || endsWithConstant()) {
            append(text);
        }
    }

    private void typeClose() {
        showingResult = false;
        int open = count('(');
        int close = count(')');
        if (open > close && (endsWithNumberPart() || endsWithConstant())) {
            append(")");
        }
    }

    private int count(char symbol) {
        int total = 0;
        for (int i = 0; i < expression.length(); i++) {
            total += expression.charAt(i) == symbol ? 1 : 0;
        }
        return total;
    }

    private boolean endsWithOperator() {
        return !expression.isEmpty() && OPERATORS.indexOf(expression.charAt(expression.length() - 1)) >= 0;
    }

    private boolean endsWithNumberPart() {
        return !expression.isEmpty()
                && NUMBER_ENDINGS.indexOf(expression.charAt(expression.length() - 1)) >= 0
                && !endsWithConstant();
    }

    private boolean endsWithConstant() {
        return expression.endsWith("π") || expression.endsWith("Ans")
                || (expression.endsWith("e") && !expression.endsWith("Ans") && isLoneE());
    }

    /** The letter e is the constant only when it does not end a word such as "exp" or "sqrt". */
    private boolean isLoneE() {
        int before = expression.length() - 2;
        return before < 0 || !Character.isLetter(expression.charAt(before));
    }

    private void clear() {
        expression = "";
        showingResult = false;
    }

    private void back() {
        showingResult = false;
        if (expression.isEmpty()) {
            return;
        }
        expression = expression.substring(0, expression.length() - lastTokenLength());
    }

    /** A function name, "Ans" or "mod" is removed as a whole, anything else one character at a time. */
    private int lastTokenLength() {
        for (String word : new String[]{"Ans", "mod"}) {
            if (expression.endsWith(word)) {
                return word.length();
            }
        }
        if (expression.endsWith("(")) {
            int start = expression.length() - 1;
            while (start > 0 && Character.isLetter(expression.charAt(start - 1))) {
                start--;
            }
            return expression.length() - start;
        }
        return 1;
    }

    private void equal() {
        if (expression.isEmpty() || showingResult) {
            return;
        }
        try {
            double value = ExpressionEvaluator.evaluate(expression, angleMode, answer);
            String shown = ResultFormat.format(value);
            history.add(0, new HistoryEntry(expression, shown));
            if (history.size() > MAX_HISTORY) {
                history.remove(history.size() - 1);
            }
            answer = value;
            expression = shown;
            showingResult = true;
        } catch (CalculationException problem) {
            error = problem.getMessage();
        }
    }

    private void addToMemory() {
        try {
            memory += expression.isEmpty() ? answer : ExpressionEvaluator.evaluate(expression, angleMode, answer);
        } catch (CalculationException problem) {
            error = problem.getMessage();
        }
    }

    private void recallMemory() {
        String value = ResultFormat.format(memory);
        typeValue(memory < 0 ? "(" + value + ")" : value);
    }

    /** A number typed right after another operand is multiplied by it instead of being glued to it. */
    private void typeValue(String text) {
        typeAfterFreshStart("");
        if (endsWithNumberPart() || endsWithConstant()) {
            append("×");
        }
        append(text);
    }
}
