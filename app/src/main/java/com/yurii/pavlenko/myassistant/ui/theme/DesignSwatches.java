package com.yurii.pavlenko.myassistant.ui.theme;

import android.content.Context;
import android.view.ContextThemeWrapper;

import com.yurii.pavlenko.myassistant.R;

/** The four colours that sum up a design, read without applying it to the screen. */
public final class DesignSwatches {

    private static final int[] ATTRIBUTES = {R.attr.maBackground, R.attr.maAccent, R.attr.maTabSelected, R.attr.maText};

    private DesignSwatches() {
    }

    /** Colours of the design in the day or night mode the context is currently in. */
    public static int[] of(Context context, AppDesign design) {
        ContextThemeWrapper themed = new ContextThemeWrapper(context, R.style.Theme_MyAssistant);
        if (design.hasOverlay()) {
            themed.getTheme().applyStyle(design.overlay, true);
        }
        int[] colours = new int[ATTRIBUTES.length];
        for (int i = 0; i < colours.length; i++) {
            colours[i] = ThemeColors.get(themed, ATTRIBUTES[i]);
        }
        return colours;
    }
}
