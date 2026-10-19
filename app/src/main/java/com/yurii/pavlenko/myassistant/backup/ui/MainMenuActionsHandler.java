package com.yurii.pavlenko.myassistant.backup.ui;

import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.yurii.pavlenko.myassistant.backup.CloudSyncManager;
import com.yurii.pavlenko.myassistant.backup.DatabaseBackupManager;
import com.yurii.pavlenko.myassistant.ui.theme.DialogStyle;

/**
 * Handler responsible for managing user actions triggered from the main overflow backup menu.
 */
public class MainMenuActionsHandler {

    /**
     * Displays the custom popup menu and handles all menu item click actions.
     */
    public static void showPopupMenu(AppCompatActivity activity, View anchorView,
                                     FragmentManager fragmentManager,
                                     ActivityResultLauncher<String> importLauncher,
                                     ActivityResultLauncher<Uri> exportFolderLauncher,
                                     Runnable appRestartCallback) {
        BackupMenuHandler.showCustomPopupMenu(activity, anchorView, new BackupMenuHandler.OnMenuActionListener() {
            @Override
            public void onExportLocal() {
                showExportDestinationDialog(activity, exportFolderLauncher);
            }

            @Override
            public void onImportLocal() {
                if (importLauncher != null) {
                    importLauncher.launch("*/*");
                }
            }

            @Override
            public void onCloudSettings() {
                new CloudSettingsDialog().show(fragmentManager, "CloudSettingsDialog");
            }

            @Override
            public void onCloudExport() {
                CloudSyncManager syncManager = new CloudSyncManager(activity);
                Toast.makeText(activity, "Uploading backup to cloud...", Toast.LENGTH_SHORT).show();

                syncManager.uploadBackup(new CloudSyncManager.SyncCallback() {
                    @Override
                    public void onSuccess(String message) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onError(String error) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(activity, error, Toast.LENGTH_LONG).show()
                        );
                    }
                });
            }

            @Override
            public void onCloudImport() {
                CloudSyncManager syncManager = new CloudSyncManager(activity);
                Toast.makeText(activity, "Restoring backup from cloud...", Toast.LENGTH_SHORT).show();

                syncManager.restoreBackup(new CloudSyncManager.SyncCallback() {
                    @Override
                    public void onSuccess(String message) {
                        activity.runOnUiThread(() -> {
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                            if (appRestartCallback != null) {
                                appRestartCallback.run();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(activity, error, Toast.LENGTH_LONG).show()
                        );
                    }
                });
            }
        });
    }

    /** The user first chooses where the backup goes: a folder on any storage, or another app. */
    private static void showExportDestinationDialog(AppCompatActivity activity, ActivityResultLauncher<Uri> exportFolderLauncher) {
        String[] destinations = {"Choose a folder (device, SD card, USB, Drive...)", "Share with an app"};
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Save backup to")
                .setItems(destinations, (d, which) -> {
                    if (which == 0) {
                        exportFolderLauncher.launch(null);
                    } else {
                        shareBackup(activity);
                    }
                })
                .create();
        dialog.show();
        DialogStyle.apply(dialog);
    }

    private static void shareBackup(AppCompatActivity activity) {
        Toast.makeText(activity, "Preparing backup...", Toast.LENGTH_SHORT).show();
        DatabaseBackupManager.prepareShareIntent(activity, shareIntent -> {
            if (shareIntent != null) {
                activity.startActivity(shareIntent);
            } else {
                Toast.makeText(activity, "Export failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
