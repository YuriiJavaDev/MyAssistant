package com.yurii.pavlenko.myassistant.backup.ui;

import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.yurii.pavlenko.myassistant.backup.SyncResult;
import com.yurii.pavlenko.myassistant.backup.BackupFailure;
import com.yurii.pavlenko.myassistant.R;
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
                Toast.makeText(activity, R.string.backup_cloud_uploading, Toast.LENGTH_SHORT).show();

                syncManager.uploadBackup(new CloudSyncManager.SyncCallback() {
                    @Override
                    public void onSuccess(SyncResult result) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(activity, BackupMessages.success(activity, result), Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onError(BackupFailure failure) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(activity, BackupMessages.failure(activity, failure), Toast.LENGTH_LONG).show()
                        );
                    }
                });
            }

            @Override
            public void onCloudImport() {
                CloudSyncManager syncManager = new CloudSyncManager(activity);
                Toast.makeText(activity, R.string.backup_cloud_restoring, Toast.LENGTH_SHORT).show();

                syncManager.restoreBackup(new CloudSyncManager.SyncCallback() {
                    @Override
                    public void onSuccess(SyncResult result) {
                        activity.runOnUiThread(() -> {
                            Toast.makeText(activity, BackupMessages.success(activity, result), Toast.LENGTH_SHORT).show();
                            if (appRestartCallback != null) {
                                appRestartCallback.run();
                            }
                        });
                    }

                    @Override
                    public void onError(BackupFailure failure) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(activity, BackupMessages.failure(activity, failure), Toast.LENGTH_LONG).show()
                        );
                    }
                });
            }
        });
    }

    /** The user first chooses where the backup goes: a folder on any storage, or another app. */
    private static void showExportDestinationDialog(AppCompatActivity activity, ActivityResultLauncher<Uri> exportFolderLauncher) {
        String[] destinations = {activity.getString(R.string.backup_destination_folder), activity.getString(R.string.backup_destination_share)};
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.backup_destination_title)
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
        Toast.makeText(activity, R.string.backup_preparing, Toast.LENGTH_SHORT).show();
        DatabaseBackupManager.prepareShareIntent(activity, shareIntent -> {
            if (shareIntent != null) {
                activity.startActivity(shareIntent);
            } else {
                Toast.makeText(activity, R.string.backup_export_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
