package com.yurii.pavlenko.myassistant.calculator.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CalculatorTest {

    private static String calc(String expression) {
        return calc(expression, AngleMode.DEGREES);
    }

    private static String calc(String expression, AngleMode mode) {
        try {
            return ResultFormat.format(ExpressionEvaluator.evaluate(expression, mode, 7));
        } catch (CalculationException problem) {
            return "ERR:" + problem.getMessage();
        }
    }

    private static CalculatorEngine engine() {
        return new CalculatorEngine(AngleMode.DEGREES, 0, () -> 0.25);
    }

    private static CalculatorEngine typed(Key... keys) {
        CalculatorEngine engine = engine();
        for (Key key : keys) {
            engine.press(key);
        }
        return engine;
    }

    @Test
    public void followsOperatorPriority() {
        assertEquals("14", calc("2+3×4"));
        assertEquals("20", calc("(2+3)×4"));
        assertEquals("-4", calc("-2^2"));
        assertEquals("512", calc("2^3^2"));
        assertEquals("0.125", calc("2^-3"));
        assertEquals("2", calc("10-4-4"));
    }

    @Test
    public void hidesBinaryRoundingNoise() {
        assertEquals("0.3", calc("0.1+0.2"));
        assertEquals("0.333333333333", calc("1÷3"));
    }

    @Test
    public void calculatesTrigonometryInBothModes() {
        assertEquals("0.707106781187", calc("sin(45)"));
        assertEquals("0", calc("cos(90)"));
        assertEquals("45", calc("atan(1)"));
        assertEquals("1", calc("sin(π÷2)", AngleMode.RADIANS));
        assertEquals("ERR:Undefined", calc("tan(90)"));
    }

    @Test
    public void calculatesPowersRootsAndLogarithms() {
        assertEquals("9", calc("3²"));
        assertEquals("27", calc("3³"));
        assertEquals("1000", calc("10^3"));
        assertEquals("4", calc("sqrt(16)"));
        assertEquals("3", calc("cbrt(27)"));
        assertEquals("2", calc("log(100)"));
        assertEquals("1", calc("ln(e)"));
        assertEquals("3628800", calc("10!"));
        assertEquals("1", calc("7 mod 3"));
        assertEquals("2", calc("-7 mod 3"));
        assertEquals("0.5", calc("50%"));
    }

    @Test
    public void multipliesNeighbouringOperands() {
        assertEquals("14", calc("2(3+4)"));
        assertEquals("6.28318530718", calc("2π"));
        assertEquals("14", calc("2Ans"));
        assertEquals("1E+20", calc("1E+20"));
    }

    @Test
    public void closesParenthesesLeftOpen() {
        assertEquals("14", calc("2×(3+4"));
        assertEquals("1", calc("sin(90"));
    }

    @Test
    public void reportsMistakesInsteadOfCrashing() {
        assertEquals("ERR:Division by zero", calc("1÷0"));
        assertEquals("ERR:Invalid input", calc("ln(0)"));
        assertEquals("ERR:Invalid input", calc("sqrt(-1)"));
        assertEquals("ERR:Invalid input", calc("asin(2)"));
        assertEquals("ERR:Invalid input", calc("2.5!"));
        assertEquals("ERR:Result is too large", calc("10^999"));
        assertEquals("ERR:Invalid expression", calc("2+"));
        assertEquals("ERR:Invalid expression", calc("2)"));
        assertEquals("ERR:Invalid expression", calc("."));
        assertEquals("ERR:Invalid expression", calc("foo(1)"));
    }

    @Test
    public void refusesHugeOrDeeplyNestedInput() {
        StringBuilder nested = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            nested.append('(');
        }
        assertEquals("ERR:Expression is too complex", calc(nested + "1"));
        assertEquals("ERR:Expression is too long", calc(new String(new char[301]).replace('\0', '1')));
    }

    @Test
    public void typesAndCalculates() {
        CalculatorEngine engine = typed(Key.TWO, Key.ADD, Key.THREE, Key.MULTIPLY, Key.FOUR);
        assertEquals("2+3×4", engine.display().getExpression());
        assertEquals("14", engine.display().getPreview());
        engine.press(Key.EQUALS);
        assertEquals("14", engine.display().getExpression());
        assertEquals("2+3×4 = 14", engine.display().getLast().toString());
    }

    @Test
    public void continuesFromTheResultOrStartsOver() {
        CalculatorEngine engine = typed(Key.TWO, Key.ADD, Key.TWO, Key.EQUALS, Key.MULTIPLY, Key.FIVE, Key.EQUALS);
        assertEquals("20", engine.display().getExpression());
        engine.press(Key.SEVEN);
        assertEquals("7", engine.display().getExpression());
    }

    @Test
    public void replacesRepeatedOperatorsButAllowsNegativeNumbers() {
        assertEquals("5×", typed(Key.FIVE, Key.ADD, Key.MULTIPLY).display().getExpression());
        assertEquals("5×-", typed(Key.FIVE, Key.MULTIPLY, Key.SUBTRACT).display().getExpression());
        assertEquals("", typed(Key.MULTIPLY).display().getExpression());
        assertEquals("-", typed(Key.SUBTRACT).display().getExpression());
    }

    @Test
    public void allowsOneDotPerNumber() {
        assertEquals("0.5", typed(Key.DOT, Key.FIVE).display().getExpression());
        assertEquals("1.5+0.2", typed(Key.ONE, Key.DOT, Key.DOT, Key.FIVE, Key.ADD, Key.DOT, Key.TWO)
                .display().getExpression());
    }

    @Test
    public void closesOnlyOpenParentheses() {
        assertEquals("(1)", typed(Key.OPEN, Key.ONE, Key.CLOSE, Key.CLOSE).display().getExpression());
        assertEquals("(", typed(Key.OPEN, Key.CLOSE).display().getExpression());
    }

    @Test
    public void backRemovesWholeFunctionNames() {
        assertEquals("2", typed(Key.TWO, Key.SIN, Key.BACK).display().getExpression());
        assertEquals("2", typed(Key.TWO, Key.MOD, Key.BACK).display().getExpression());
        assertEquals("1", typed(Key.ONE, Key.TWO, Key.BACK).display().getExpression());
        assertEquals("", typed(Key.ANS, Key.BACK).display().getExpression());
    }

    @Test
    public void keepsMemory() {
        CalculatorEngine engine = typed(Key.ONE, Key.FOUR, Key.SEVEN, Key.MEMORY_ADD, Key.CLEAR);
        assertEquals("147", engine.display().getMemory());
        engine.press(Key.MEMORY_RECALL);
        assertEquals("147", engine.display().getExpression());
        engine.press(Key.MEMORY_RECALL);
        assertEquals("147×147", engine.display().getExpression());
        engine.press(Key.MEMORY_CLEAR);
        assertTrue(engine.display().getMemory() == null);
    }

    @Test
    public void recallsNegativeMemoryInParentheses() {
        CalculatorEngine engine = typed(Key.FIVE, Key.SUBTRACT, Key.NINE, Key.EQUALS, Key.MEMORY_ADD,
                Key.CLEAR, Key.TWO, Key.SUBTRACT, Key.MEMORY_RECALL);
        assertEquals("2-(-4)", engine.display().getExpression());
        assertEquals("6", engine.display().getPreview());
    }

    @Test
    public void usesPreviousAnswerAndRandomValue() {
        CalculatorEngine engine = typed(Key.TWO, Key.ADD, Key.THREE, Key.EQUALS, Key.CLEAR, Key.ANS, Key.MULTIPLY, Key.TWO);
        assertEquals("10", engine.display().getPreview());
        engine.press(Key.CLEAR);
        engine.press(Key.RANDOM);
        assertEquals("0.25", engine.display().getExpression());
    }

    @Test
    public void showsErrorsAndKeepsTheExpression() {
        CalculatorEngine engine = typed(Key.ONE, Key.DIVIDE, Key.ZERO, Key.EQUALS);
        assertEquals("Division by zero", engine.display().getError());
        assertEquals("1÷0", engine.display().getExpression());
        engine.press(Key.BACK);
        assertTrue(engine.display().getError() == null);
    }

    @Test
    public void switchesAngleMode() {
        CalculatorEngine engine = typed(Key.SIN, Key.THREE, Key.ZERO);
        assertEquals("0.5", engine.display().getPreview());
        engine.toggleAngleMode();
        assertEquals(AngleMode.RADIANS, engine.display().getAngleMode());
        assertEquals("-0.988031624093", engine.display().getPreview());
    }

    @Test
    public void keepsNewestHistoryFirstAndRestoresEntries() {
        CalculatorEngine engine = typed(Key.ONE, Key.ADD, Key.ONE, Key.EQUALS, Key.CLEAR, Key.TWO, Key.ADD, Key.TWO, Key.EQUALS);
        assertEquals("2+2 = 4", engine.getHistory().get(0).toString());
        assertEquals("1+1 = 2", engine.getHistory().get(1).toString());
        engine.restore("1+1");
        assertEquals("1+1", engine.display().getExpression());
        assertEquals("2", engine.display().getPreview());
    }

    @Test
    public void closesOpenParenthesesWhenCalculating() {
        CalculatorEngine engine = typed(Key.SIN, Key.THREE, Key.ZERO, Key.EQUALS);
        assertEquals("sin(30) = 0.5", engine.display().getLast().toString());
        assertEquals("0.5", engine.display().getExpression());
        CalculatorEngine nested = typed(Key.TWO, Key.MULTIPLY, Key.OPEN, Key.THREE, Key.ADD, Key.OPEN, Key.ONE, Key.EQUALS);
        assertEquals("2×(3+(1)) = 8", nested.display().getLast().toString());
    }

    /** Presses the keys and returns what the user sees: the result, or the error if there is one. */
    private static String run(Key... keys) {
        CalculatorEngine engine = typed(keys);
        CalculatorDisplay display = engine.display();
        return display.getError() != null ? "ERR:" + display.getError() : display.getExpression();
    }

    @Test
    public void everyKeyDoesWhatItSays() {
        assertEquals("0123456789", run(Key.ZERO, Key.ONE, Key.TWO, Key.THREE, Key.FOUR, Key.FIVE, Key.SIX,
                Key.SEVEN, Key.EIGHT, Key.NINE));
        assertEquals("7", run(Key.NINE, Key.SUBTRACT, Key.TWO, Key.EQUALS));
        assertEquals("11", run(Key.NINE, Key.ADD, Key.TWO, Key.EQUALS));
        assertEquals("18", run(Key.NINE, Key.MULTIPLY, Key.TWO, Key.EQUALS));
        assertEquals("4.5", run(Key.NINE, Key.DIVIDE, Key.TWO, Key.EQUALS));
        assertEquals("1", run(Key.NINE, Key.MOD, Key.TWO, Key.EQUALS));
        assertEquals("81", run(Key.NINE, Key.POWER, Key.TWO, Key.EQUALS));
        assertEquals("0.09", run(Key.NINE, Key.PERCENT, Key.EQUALS));
        assertEquals("81", run(Key.NINE, Key.SQUARE, Key.EQUALS));
        assertEquals("729", run(Key.NINE, Key.CUBE, Key.EQUALS));
        assertEquals("362880", run(Key.NINE, Key.FACTORIAL, Key.EQUALS));
        assertEquals("2", run(Key.OPEN, Key.ONE, Key.ADD, Key.ONE, Key.CLOSE, Key.EQUALS));
        assertEquals("0.5", run(Key.SIN, Key.THREE, Key.ZERO, Key.EQUALS));
        assertEquals("0.5", run(Key.COS, Key.SIX, Key.ZERO, Key.EQUALS));
        assertEquals("1", run(Key.TAN, Key.FOUR, Key.FIVE, Key.EQUALS));
        assertEquals("30", run(Key.ASIN, Key.DOT, Key.FIVE, Key.EQUALS));
        assertEquals("60", run(Key.ACOS, Key.DOT, Key.FIVE, Key.EQUALS));
        assertEquals("45", run(Key.ATAN, Key.ONE, Key.EQUALS));
        assertEquals("2", run(Key.LN, Key.E, Key.POWER, Key.TWO, Key.EQUALS));
        assertEquals("3", run(Key.LOG, Key.ONE, Key.ZERO, Key.ZERO, Key.ZERO, Key.EQUALS));
        assertEquals("1", run(Key.EXP, Key.ZERO, Key.EQUALS));
        assertEquals("3", run(Key.SQRT, Key.NINE, Key.EQUALS));
        assertEquals("2", run(Key.CBRT, Key.EIGHT, Key.EQUALS));
        assertEquals("5", run(Key.ABS, Key.SUBTRACT, Key.FIVE, Key.EQUALS));
        assertEquals("1000", run(Key.TEN_POWER, Key.THREE, Key.EQUALS));
        assertEquals("0.25", run(Key.RECIPROCAL, Key.FOUR, Key.EQUALS));
        assertEquals("3.14159265359", run(Key.PI, Key.EQUALS));
        assertEquals("2.71828182846", run(Key.E, Key.EQUALS));
        assertEquals("0.25", run(Key.RANDOM, Key.EQUALS));
        assertEquals("", run(Key.ONE, Key.CLEAR));
        assertEquals("1", run(Key.ONE, Key.TWO, Key.BACK));
        assertEquals("Ans", run(Key.ONE, Key.ADD, Key.TWO, Key.EQUALS, Key.CLEAR, Key.ANS));
    }

    @Test
    public void everyKeyHasALabelAndANameThatFindsIt() {
        for (Key key : Key.values()) {
            assertTrue(!key.label().isEmpty());
            assertEquals(key, Key.named(key.name()));
        }
        assertTrue(Key.named("NOT_A_KEY") == null);
    }

    @Test
    public void tellsTheUserAboutMistakes() {
        assertEquals("ERR:Division by zero", run(Key.FIVE, Key.DIVIDE, Key.ZERO, Key.EQUALS));
        assertEquals("ERR:Division by zero", run(Key.FIVE, Key.MOD, Key.ZERO, Key.EQUALS));
        assertEquals("ERR:Invalid input", run(Key.LN, Key.ZERO, Key.EQUALS));
        assertEquals("ERR:Invalid input", run(Key.LOG, Key.SUBTRACT, Key.ONE, Key.EQUALS));
        assertEquals("ERR:Invalid input", run(Key.SQRT, Key.SUBTRACT, Key.FOUR, Key.EQUALS));
        assertEquals("ERR:Invalid input", run(Key.ASIN, Key.TWO, Key.EQUALS));
        assertEquals("ERR:Invalid input", run(Key.ACOS, Key.TWO, Key.EQUALS));
        assertEquals("ERR:Invalid input", run(Key.TWO, Key.DOT, Key.FIVE, Key.FACTORIAL, Key.EQUALS));
        assertEquals("ERR:Division by zero", run(Key.ZERO, Key.POWER, Key.SUBTRACT, Key.ONE, Key.EQUALS));
        assertEquals("ERR:Undefined", run(Key.TAN, Key.NINE, Key.ZERO, Key.EQUALS));
        assertEquals("ERR:Result is too large", run(Key.ONE, Key.ZERO, Key.POWER, Key.NINE, Key.NINE, Key.NINE, Key.EQUALS));
        assertEquals("ERR:Result is too large", run(Key.ONE, Key.SEVEN, Key.ONE, Key.FACTORIAL, Key.EQUALS));
        assertEquals("ERR:Invalid expression", run(Key.ONE, Key.ADD, Key.EQUALS));
        assertEquals("ERR:Invalid expression", run(Key.OPEN, Key.EQUALS));
        assertEquals("ERR:Invalid expression", run(Key.SIN, Key.EQUALS));
        assertEquals("ERR:Invalid expression", run(Key.DOT, Key.CLEAR, Key.SUBTRACT, Key.EQUALS));
    }

    @Test
    public void ignoresKeysThatMakeNoSenseHere() {
        assertEquals("", run(Key.EQUALS));
        assertEquals("", run(Key.PERCENT));
        assertEquals("", run(Key.FACTORIAL));
        assertEquals("", run(Key.CLOSE));
        assertEquals("", run(Key.BACK));
        assertEquals("", run(Key.DIVIDE));
        assertEquals("1", run(Key.ONE, Key.EQUALS, Key.EQUALS));
    }
}
