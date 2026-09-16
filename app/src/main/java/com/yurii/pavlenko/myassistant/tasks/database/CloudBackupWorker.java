package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Worker responsible for executing automated daily database backups to the cloud
 * in the background using WorkManager, delegating sync operations to CloudSyncManager.
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

        // Step 1: Verify if auto-backup is enabled by the user
        if (!configManager.isAutoBackupEnabled()) {
            return Result.success();
        }

        // Step 2: Validate if cloud URL is configured
        String urlStr = configManager.getUrl();
        if (urlStr == null || urlStr.isEmpty()) {
            return Result.failure();
        }

        CloudSyncManager syncManager = new CloudSyncManager(context);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean isSuccess = new AtomicBoolean(false);

        // Step 3: Trigger database upload using CloudSyncManager
        syncManager.uploadDatabase(new CloudSyncManager.SyncCallback() {
            @Override
            public void onSuccess(String message) {
                isSuccess.set(true);
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                isSuccess.set(false);
                latch.countDown();
            }
        });

        try {
            // Step 4: Await asynchronous upload completion to fit WorkManager synchronous contract
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Result.failure();
        }

        // Step 5: Return appropriate WorkManager result
        return isSuccess.get() ? Result.success() : Result.retry();
    }
}