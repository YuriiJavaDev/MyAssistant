package com.yurii.pavlenko.myassistant.ui.theme;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.yurii.pavlenko.myassistant.R;

/** Gives a dialog the look of the current design: its background, texts and buttons. */
public final class DialogStyle {

    private static final int BUTTON_PADDING_DP = 16;
    private static final int BUTTON_GAP_DP = 8;

    private DialogStyle() {
    }

    /** Shows the dialog the builder describes and styles it. */
    public static AlertDialog show(AlertDialog.Builder builder) {
        AlertDialog dialog = builder.create();
        dialog.show();
        apply(dialog);
        return dialog;
    }

    /** Styles a dialog that is already showing, because its buttons exist only from then on. */
    public static void apply(AlertDialog dialog) {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_spinner_dropdown);
        }
        int textColor = ThemeColors.get(dialog.getContext(), R.attr.maText);
        colorText(dialog.findViewById(androidx.appcompat.R.id.alertTitle), textColor);
        colorText(dialog.findViewById(android.R.id.message), textColor);
        colorListItems(dialog.getListView(), textColor);
        styleButton(dialog.getButton(AlertDialog.BUTTON_POSITIVE), true);
        styleButton(dialog.getButton(AlertDialog.BUTTON_NEUTRAL), true);
        styleButton(dialog.getButton(AlertDialog.BUTTON_NEGATIVE), false);
    }

    private static void colorText(View view, int color) {
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        }
    }

    /** List rows are created while scrolling, so each one is coloured as it appears. */
    private static void colorListItems(ListView list, int color) {
        if (list == null) {
            return;
        }
        list.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                colorText(child, color);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });
        for (int i = 0; i < list.getChildCount(); i++) {
            colorText(list.getChildAt(i), color);
        }
    }

    private static void styleButton(Button button, boolean gapBefore) {
        if (button == null) {
            return;
        }
        float density = button.getResources().getDisplayMetrics().density;
        int padding = (int) (BUTTON_PADDING_DP * density);
        button.setBackgroundTintList(null);
        button.setBackgroundResource(R.drawable.button_selector);
        button.setTextColor(ThemeColors.get(button.getContext(), R.attr.maButtonLabel));
        button.setAllCaps(false);
        button.setPadding(padding, button.getPaddingTop(), padding, button.getPaddingBottom());
        if (gapBefore && button.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
            params.leftMargin = (int) (BUTTON_GAP_DP * density);
            button.setLayoutParams(params);
        }
    }
}
