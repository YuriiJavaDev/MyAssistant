package com.yurii.pavlenko.myassistant.calculator.ui;

import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yurii.pavlenko.myassistant.calculator.logic.Key;

import java.util.function.Consumer;

/**
 * Connects every key of a layout to the calculator. A key is a button whose tag is the name of a
 * {@link Key}; its label comes from the key, so portrait and landscape layouts share one definition.
 */
final class KeypadBinder {

    private KeypadBinder() {
    }

    static void bind(View root, Consumer<Key> onKey) {
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                bind(group.getChildAt(i), onKey);
            }
        } else if (root instanceof TextView && root.getTag() instanceof String) {
            Key key = Key.named((String) root.getTag());
            if (key != null) {
                ((TextView) root).setText(key.label());
                root.setOnClickListener(view -> {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    onKey.accept(key);
                });
            }
        }
    }
}
