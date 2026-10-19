package com.yurii.pavlenko.myassistant.scan.ui;

import android.content.Context;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.scan.export.ExportFormat;
import com.yurii.pavlenko.myassistant.scan.model.ScanDocument;
import com.yurii.pavlenko.myassistant.tasks.ui.dialogs.DeleteConfirmationDialog;
import com.yurii.pavlenko.myassistant.ui.theme.DialogStyle;

/** The menu shown when a document in the list is tapped, including the rename and delete confirmations. */
final class ScanActionsDialog {

    interface Listener {
        void onOpenPages(ScanDocument document);

        void onShare(ScanDocument document, ExportFormat format);

        void onSavePdfTo(ScanDocument document);

        void onRename(ScanDocument document, String title);

        void onDelete(ScanDocument document);
    }

    private ScanActionsDialog() {
    }

    static void show(Context context, ScanDocument document, Listener listener) {
        String[] actions = context.getResources().getStringArray(R.array.scan_actions);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(document.getTitle())
                .setItems(actions, (d, which) -> perform(context, document, which, listener))
                .create();
        dialog.show();
        DialogStyle.apply(dialog);
    }

    /** Order matches the scan_actions array in strings.xml. */
    private static void perform(Context context, ScanDocument document, int action, Listener listener) {
        switch (action) {
            case 0: listener.onOpenPages(document); break;
            case 1: listener.onShare(document, ExportFormat.PDF); break;
            case 2: listener.onShare(document, ExportFormat.JPEG); break;
            case 3: listener.onSavePdfTo(document); break;
            case 4: showRename(context, document, listener); break;
            default: confirmDelete(context, document, listener); break;
        }
    }

    private static void showRename(Context context, ScanDocument document, Listener listener) {
        EditText input = new EditText(context);
        input.setText(document.getTitle());
        input.setSelection(input.getText().length());
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.scan_rename_title)
                .setView(input)
                .setPositiveButton(R.string.scan_rename_confirm, (d, which) -> {
                    String title = input.getText().toString().trim();
                    if (!title.isEmpty()) {
                        listener.onRename(document, title);
                    }
                })
                .setNegativeButton(R.string.common_cancel, null)
                .create();
        dialog.show();
        DialogStyle.apply(dialog);
    }

    private static void confirmDelete(Context context, ScanDocument document, Listener listener) {
        DeleteConfirmationDialog.showCustom(context, "Delete Confirmation",
                "Delete \"" + document.getTitle() + "\" with all its pages? This action cannot be undone!",
                "Delete", () -> listener.onDelete(document));
    }

    /** Warns before every document is deleted; with nothing to delete it only says so. */
    static void confirmClearAll(Context context, int documentCount, int pageCount, Runnable onConfirmed) {
        if (documentCount == 0) {
            DeleteConfirmationDialog.show(context, false, onConfirmed);
            return;
        }
        DeleteConfirmationDialog.showCustom(context, "Delete Confirmation",
                "Delete all " + documentCount + (documentCount == 1 ? " document" : " documents")
                        + " with " + pageCount + (pageCount == 1 ? " page" : " pages") + "? This action cannot be undone!",
                "Delete", onConfirmed);
    }

}
