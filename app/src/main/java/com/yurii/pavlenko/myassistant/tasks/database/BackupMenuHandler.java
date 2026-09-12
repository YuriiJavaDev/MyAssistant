package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Intent;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;

import com.yurii.pavlenko.myassistant.R;

/**
 * Utility class responsible for handling database backup and restore menu actions.
 */
public class BackupMenuHandler {

    public static boolean handleMenuAction(
            MenuItem item,
            ExportTrigger exportTrigger,
            ActivityResultLauncher<String> importDatabaseLauncher) {

        int id = item.getItemId();

        if (id == R.id.action_export_db) {
            exportTrigger.launchExport();
            return true;
        } else if (id == R.id.action_import_db) {
            importDatabaseLauncher.launch("*/*");
            return true;
        }

        return false;
    }

    public interface ExportTrigger {
        void launchExport();
    }
}