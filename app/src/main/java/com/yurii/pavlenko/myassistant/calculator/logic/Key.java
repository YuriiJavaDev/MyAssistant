package com.yurii.pavlenko.myassistant.calculator.logic;

/** Every key of the calculator: what it is called on the screen, what it types and how it behaves. */
public enum Key {
    ZERO("0", "0", Kind.DIGIT), ONE("1", "1", Kind.DIGIT), TWO("2", "2", Kind.DIGIT),
    THREE("3", "3", Kind.DIGIT), FOUR("4", "4", Kind.DIGIT), FIVE("5", "5", Kind.DIGIT),
    SIX("6", "6", Kind.DIGIT), SEVEN("7", "7", Kind.DIGIT), EIGHT("8", "8", Kind.DIGIT),
    NINE("9", "9", Kind.DIGIT),
    DOT(".", ".", Kind.DOT),

    ADD("+", "+", Kind.OPERATOR), SUBTRACT("−", "-", Kind.OPERATOR),
    MULTIPLY("×", "×", Kind.OPERATOR), DIVIDE("÷", "÷", Kind.OPERATOR),
    POWER("x^y", "^", Kind.OPERATOR), MOD("mod", "mod", Kind.OPERATOR),

    PERCENT("%", "%", Kind.POSTFIX), SQUARE("x²", "²", Kind.POSTFIX),
    CUBE("x³", "³", Kind.POSTFIX), FACTORIAL("n!", "!", Kind.POSTFIX),

    OPEN("(", "(", Kind.PREFIX), CLOSE(")", ")", Kind.CLOSE),
    SIN("sin", "sin(", Kind.PREFIX), COS("cos", "cos(", Kind.PREFIX), TAN("tan", "tan(", Kind.PREFIX),
    ASIN("asin", "asin(", Kind.PREFIX), ACOS("acos", "acos(", Kind.PREFIX), ATAN("atan", "atan(", Kind.PREFIX),
    LN("ln", "ln(", Kind.PREFIX), LOG("log", "log(", Kind.PREFIX), EXP("exp", "exp(", Kind.PREFIX),
    SQRT("sqrt", "sqrt(", Kind.PREFIX), CBRT("cbrt", "cbrt(", Kind.PREFIX), ABS("abs", "abs(", Kind.PREFIX),
    TEN_POWER("10^x", "10^", Kind.PREFIX), RECIPROCAL("1/x", "1÷(", Kind.PREFIX),
    PI("π", "π", Kind.PREFIX), E("e", "e", Kind.PREFIX), ANS("Ans", "Ans", Kind.PREFIX),
    RANDOM("Rand", "", Kind.RANDOM),

    CLEAR("C", "", Kind.CLEAR), BACK("Back", "", Kind.BACK), EQUALS("=", "", Kind.EQUALS),
    MEMORY_ADD("M+", "", Kind.MEMORY_ADD), MEMORY_RECALL("MR", "", Kind.MEMORY_RECALL),
    MEMORY_CLEAR("MC", "", Kind.MEMORY_CLEAR);

    /** How a key acts on the expression. */
    enum Kind {
        DIGIT, DOT, OPERATOR, POSTFIX, PREFIX, CLOSE, RANDOM, CLEAR, BACK, EQUALS,
        MEMORY_ADD, MEMORY_RECALL, MEMORY_CLEAR
    }

    private final String label;
    private final String text;
    private final Kind kind;

    Key(String label, String text, Kind kind) {
        this.label = label;
        this.text = text;
        this.kind = kind;
    }

    public String label() {
        return label;
    }

    String text() {
        return text;
    }

    Kind kind() {
        return kind;
    }

    /** Returns null when the name is not a key. */
    public static Key named(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException notAKey) {
            return null;
        }
    }
}
