package com.yurii.pavlenko.myassistant.calculator.ui;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.calculator.logic.CalcError;

/** Maps a calculation error to the text shown to the user. */
final class CalcErrorText {

    private CalcErrorText() {
    }

    @StringRes
    static int of(@NonNull CalcError error) {
        switch (error) {
            case INVALID_INPUT:
                return R.string.calculator_error_invalid_input;
            case INVALID_EXPRESSION:
                return R.string.calculator_error_invalid_expression;
            case DIVISION_BY_ZERO:
                return R.string.calculator_error_division_by_zero;
            case RESULT_TOO_LARGE:
                return R.string.calculator_error_result_too_large;
            case UNDEFINED:
                return R.string.calculator_error_undefined;
            case EXPRESSION_TOO_LONG:
                return R.string.calculator_error_expression_too_long;
            default:
                return R.string.calculator_error_expression_too_complex;
        }
    }
}
