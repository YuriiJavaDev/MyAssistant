package com.yurii.pavlenko.myassistant.scan.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.scan.export.ExportFormat;
import com.yurii.pavlenko.myassistant.scan.model.ScanItem;
import com.yurii.pavlenko.myassistant.tasks.ui.dialogs.DeleteConfirmationDialog;

/** The menu shown when a scan in the list is tapped, including the rename and delete confirmations. */
final class ScanActionsDialog {

    interface Listener {
        void onOpen(ScanItem item);

        void onShare(ScanItem item, ExportFormat format);

        void onSaveTo(ScanItem item, ExportFormat format);

        void onRename(ScanItem item, String title);

        void onDelete(ScanItem item);
    }

    private ScanActionsDialog() {
    }

    static void show(Context context, ScanItem item, Listener listener) {
        String[] actions = context.getResources().getStringArray(R.array.scan_actions);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(item.getTitle())
                .setItems(actions, (d, which) -> perform(context, item, which, listener))
                .create();
        dialog.show();
        applyAppBackground(dialog);
    }

    /** Order matches the scan_actions array in strings.xml. */
    private static void perform(Context context, ScanItem item, int action, Listener listener) {
        switch (action) {
            case 0: listener.onOpen(item); break;
            case 1: listener.onShare(item, ExportFormat.PDF); break;
            case 2: listener.onShare(item, ExportFormat.JPEG); break;
            case 3: listener.onSaveTo(item, ExportFormat.PDF); break;
            case 4: listener.onSaveTo(item, ExportFormat.JPEG); break;
            case 5: showRename(context, item, listener); break;
            default: confirmDelete(context, item, listener); break;
        }
    }

    private static void showRename(Context context, ScanItem item, Listener listener) {
        EditText input = new EditText(context);
        input.setText(item.getTitle());
        input.setSelection(input.getText().length());
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.scan_rename_title)
                .setView(input)
                .setPositiveButton(R.string.scan_rename_confirm, (d, which) -> {
                    String title = input.getText().toString().trim();
                    if (!title.isEmpty()) {
                        listener.onRename(item, title);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
        applyAppBackground(dialog);
    }

    private static void confirmDelete(Context context, ScanItem item, Listener listener) {
        DeleteConfirmationDialog.showCustom(context, "Delete Confirmation",
                "Delete \"" + item.getTitle() + "\"? This action cannot be undone!",
                "Delete", () -> listener.onDelete(item));
    }

    private static void applyAppBackground(AlertDialog dialog) {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_spinner_dropdown);
        }
    }
}
