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
}
