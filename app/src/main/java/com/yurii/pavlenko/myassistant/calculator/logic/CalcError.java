package com.yurii.pavlenko.myassistant.calculator.logic;

/** Why a calculation could not be done; the wording is chosen by the screen, in the language of the user. */
public enum CalcError {
    INVALID_INPUT,
    INVALID_EXPRESSION,
    DIVISION_BY_ZERO,
    RESULT_TOO_LARGE,
    UNDEFINED,
    EXPRESSION_TOO_LONG,
    EXPRESSION_TOO_COMPLEX
}
