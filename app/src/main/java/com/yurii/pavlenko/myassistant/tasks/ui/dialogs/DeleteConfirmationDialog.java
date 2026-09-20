package com.yurii.pavlenko.myassistant.tasks.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.yurii.pavlenko.myassistant.R;

public class DeleteConfirmationDialog {

    public static AlertDialog show(Context context, boolean canDelete, Runnable onConfirmed) {
        if (!canDelete) {
            Toast.makeText(context, "No objects found to delete!", Toast.LENGTH_SHORT).show();
            return null;
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this item? This action cannot be undone!")
                .setPositiveButton("Delete", (dialogInterface, which) -> onConfirmed.run())
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_spinner_dropdown);
        }

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        int paddingHorizontal = (int) (16 * context.getResources().getDisplayMetrics().density);
        int marginHorizontal = (int) (8 * context.getResources().getDisplayMetrics().density);

        if (positiveButton != null) {
            positiveButton.setBackgroundTintList(null);
            positiveButton.setBackgroundResource(R.drawable.button_selector);
            positiveButton.setTextColor(ContextCompat.getColor(context, R.color.text_color_btn));
            positiveButton.setAllCaps(false);

            positiveButton.setPadding(paddingHorizontal, positiveButton.getPaddingTop(), paddingHorizontal, positiveButton.getPaddingBottom());

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) positiveButton.getLayoutParams();
            params.leftMargin = marginHorizontal;
            positiveButton.setLayoutParams(params);
        }

        if (negativeButton != null) {
            negativeButton.setBackgroundTintList(null);
            negativeButton.setBackgroundResource(R.drawable.button_selector);
            negativeButton.setTextColor(ContextCompat.getColor(context, R.color.text_color_btn));
            negativeButton.setAllCaps(false);
            negativeButton.setPadding(paddingHorizontal, negativeButton.getPaddingTop(), paddingHorizontal, negativeButton.getPaddingBottom());
        }

        return dialog;
    }
}