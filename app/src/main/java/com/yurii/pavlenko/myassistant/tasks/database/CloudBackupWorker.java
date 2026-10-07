package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;

/** Uploads the local database to the cloud on a schedule when automatic backup is enabled. */
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

        try {
            new CloudSyncManager(context).uploadDatabaseBlocking();
            return Result.success();
        } catch (IOException e) {
            return Result.retry();
        }
    }
}