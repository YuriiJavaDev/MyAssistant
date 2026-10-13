package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;
import android.net.ConnectivityManager;

import androidx.annotation.NonNull;
import androidx.core.net.ConnectivityManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;

/**
 * Uploads the local backup to the cloud on a schedule when automatic backup is enabled.
 * Tasks and steps are small and always uploaded; the scan archive can be large, so it goes only when it has changed
 * and the connection is not metered.
 */
public class CloudBackupWorker extends Worker {

    public CloudBackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        CloudConfigManager configManager = new CloudConfigManager(context);

        if (!configManager.isAutoBackupEnabled()) {
            return Result.success();
        }
        if (configManager.getUrl().isEmpty()) {
            return Result.failure();
        }

        CloudSyncManager syncManager = new CloudSyncManager(context);
        try {
            syncManager.uploadDatabaseBlocking(ManagedDatabase.TASKS);
            if (syncManager.hasLocalDatabase(ManagedDatabase.STEPS)) {
                syncManager.uploadDatabaseBlocking(ManagedDatabase.STEPS);
            }
            if (!isConnectionMetered(context) && syncManager.scansNeedUpload()) {
                syncManager.uploadScansBlocking();
            }
            return Result.success();
        } catch (IOException e) {
            return Result.retry();
        }
    }

    private static boolean isConnectionMetered(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager == null || ConnectivityManagerCompat.isActiveNetworkMetered(manager);
    }
}
