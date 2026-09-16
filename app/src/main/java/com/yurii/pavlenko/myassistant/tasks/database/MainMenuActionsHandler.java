package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

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
                                     Runnable appRestartCallback) {
        BackupMenuHandler.showCustomPopupMenu(activity, anchorView, new BackupMenuHandler.OnMenuActionListener() {
            @Override
            public void onExportLocal() {
                Intent shareIntent = DatabaseBackupManager.getExportShareIntent(activity);
                if (shareIntent != null) {
                    activity.startActivity(shareIntent);
                } else {
                    Toast.makeText(activity, "Export failed", Toast.LENGTH_SHORT).show();
                }
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
                Toast.makeText(activity, "Exporting database to cloud...", Toast.LENGTH_SHORT).show();

                syncManager.uploadDatabase(new CloudSyncManager.SyncCallback() {
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
                Toast.makeText(activity, "Importing database from cloud...", Toast.LENGTH_SHORT).show();

                syncManager.downloadDatabase(new CloudSyncManager.SyncCallback() {
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
}