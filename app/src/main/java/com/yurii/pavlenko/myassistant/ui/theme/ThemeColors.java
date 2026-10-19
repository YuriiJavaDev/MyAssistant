package com.yurii.pavlenko.myassistant.ui.theme;

import android.content.Context;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;

import com.google.android.material.color.MaterialColors;

/** Reads a colour of the current design from the theme of a context. */
public final class ThemeColors {

    private ThemeColors() {
    }

    /** The colour behind a design attribute such as {@code R.attr.maText}; the context must carry the app theme. */
    @ColorInt
    public static int get(Context context, @AttrRes int attribute) {
        return MaterialColors.getColor(context, attribute, ThemeColors.class.getSimpleName());
    }
}
