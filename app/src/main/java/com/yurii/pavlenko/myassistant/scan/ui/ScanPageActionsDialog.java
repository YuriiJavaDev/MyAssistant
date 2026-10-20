package com.yurii.pavlenko.myassistant.scan.ui;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.scan.model.ScanPage;
import com.yurii.pavlenko.myassistant.tasks.ui.dialogs.DeleteConfirmationDialog;
import com.yurii.pavlenko.myassistant.ui.theme.DialogStyle;

/** The menu shown when a page of a document is tapped. */
final class ScanPageActionsDialog {

    interface Listener {
        void onView(ScanPage page);

        void onSaveJpegTo(ScanPage page);

        void onMove(ScanPage page, int direction);

        void onDelete(ScanPage page);
    }

    private ScanPageActionsDialog() {
    }

    static void show(Context context, String title, ScanPage page, Listener listener) {
        String[] actions = context.getResources().getStringArray(R.array.scan_page_actions);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setItems(actions, (d, which) -> perform(context, page, which, listener))
                .create();
        dialog.show();
        DialogStyle.apply(dialog);
    }

    /** Order matches the scan_page_actions array in strings.xml. */
    private static void perform(Context context, ScanPage page, int action, Listener listener) {
        switch (action) {
            case 0: listener.onView(page); break;
            case 1: listener.onSaveJpegTo(page); break;
            case 2: listener.onMove(page, -1); break;
            case 3: listener.onMove(page, 1); break;
            default: confirmDelete(context, page, listener); break;
        }
    }

    private static void confirmDelete(Context context, ScanPage page, Listener listener) {
        DeleteConfirmationDialog.showCustom(context, context.getString(R.string.tasks_delete_title),
                context.getString(R.string.scan_delete_page_message),
                context.getString(R.string.tasks_delete_button), () -> listener.onDelete(page));
    }

}
