package com.yurii.pavlenko.myassistant.tasks.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

public class DeleteConfirmationDialog {

    public static void show(Context context, boolean canDelete, Runnable onConfirmed) {
        if (!canDelete) {
            Toast.makeText(context, "No objects found to delete!", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this item? This action cannot be undone!")
                .setPositiveButton("Delete", (dialog, which) -> onConfirmed.run())
                .setNegativeButton("Cancel", null)
                .show();
    }
}