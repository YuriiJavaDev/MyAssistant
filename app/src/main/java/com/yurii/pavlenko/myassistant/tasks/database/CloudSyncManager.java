package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;

import com.yurii.pavlenko.myassistant.scan.backup.ScanBackupArchive;
import com.yurii.pavlenko.myassistant.scan.backup.ScanBackupRestorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Uploads the local backup (task database and scan archive) to a WebDAV server and restores it from there. */
public class CloudSyncManager {

    private static final int TIMEOUT_MS = 10_000;
    private static final int HTTP_NOT_FOUND = 404;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public interface SyncCallback {
        void onSuccess(String message);

        void onError(String error);
    }

    /** Returns the message to show when the action succeeds. */
    private interface IoAction {
        String run() throws IOException;
    }

    private final Context context;
    private final CloudConfigManager configManager;

    public CloudSyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.configManager = new CloudConfigManager(this.context);
    }

    public void uploadBackup(SyncCallback callback) {
        runAsync(() -> {
            uploadTasksBlocking();
            if (ScanBackupArchive.hasData(context)) {
                uploadScansBlocking();
            }
            return "Backup successfully uploaded to cloud";
        }, "Upload error: ", callback);
    }

    /**
     * Restores tasks and scans from the cloud. Each file is downloaded and verified completely
     * before the local data is replaced, so a failed download leaves local data untouched.
     */
    public void restoreBackup(SyncCallback callback) {
        runAsync(() -> {
            downloadTasksBlocking();
            boolean scansRestored = downloadScansBlocking();
            return scansRestored
                    ? "Backup successfully restored from cloud"
                    : "Tasks restored from cloud (no scan backup found there)";
        }, "Download error: ", callback);
    }

    /** Blocking upload for background workers that already run off the main thread. */
    public void uploadTasksBlocking() throws IOException {
        File dbFile = DatabaseFileManager.getDatabaseFile(context);
        if (!dbFile.exists()) {
            throw new IOException("Local database file not found");
        }
        upload(DatabaseFileManager.BACKUP_FILE_NAME, dbFile);
    }

    /** The archive is built in the cache first, because the upload needs to know its size. */
    public void uploadScansBlocking() throws IOException {
        String signature = ScanBackupArchive.signature(context);
        File archive = new File(DatabaseBackupManager.cacheFolder(context), ScanBackupArchive.FILE_NAME);
        try {
            try (OutputStream out = new FileOutputStream(archive)) {
                ScanBackupArchive.write(context, out);
            }
            upload(ScanBackupArchive.FILE_NAME, archive);
        } finally {
            archive.delete();
        }
        configManager.saveUploadedScanSignature(signature);
    }

    /** True when the scans have changed since they were last uploaded. */
    public boolean scansNeedUpload() {
        return ScanBackupArchive.hasData(context)
                && !ScanBackupArchive.signature(context).equals(configManager.getUploadedScanSignature());
    }

    private void downloadTasksBlocking() throws IOException {
        HttpURLConnection connection = openConnection(DatabaseFileManager.BACKUP_FILE_NAME, "GET");
        try {
            requireSuccess(connection);
            File staged;
            try (InputStream in = connection.getInputStream()) {
                staged = DatabaseFileManager.stage(context, in);
            }
            DatabaseFileManager.replaceWith(context, staged);
        } finally {
            connection.disconnect();
        }
    }

    /** Returns false when the server has no scan archive, which is normal for a backup made before scans existed. */
    private boolean downloadScansBlocking() throws IOException {
        HttpURLConnection connection = openConnection(ScanBackupArchive.FILE_NAME, "GET");
        try {
            if (connection.getResponseCode() == HTTP_NOT_FOUND) {
                return false;
            }
            requireSuccess(connection);
            try (InputStream in = connection.getInputStream()) {
                ScanBackupRestorer.restore(context, in);
            }
            return true;
        } finally {
            connection.disconnect();
        }
    }

    private void upload(String fileName, File file) throws IOException {
        HttpURLConnection connection = openConnection(fileName, "PUT");
        try {
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(file.length());
            try (InputStream in = new FileInputStream(file);
                 OutputStream out = connection.getOutputStream()) {
                DatabaseFileManager.copy(in, out);
            }
            requireSuccess(connection);
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection openConnection(String fileName, String method) throws IOException {
        String baseUrl = configManager.getUrl();
        if (baseUrl.isEmpty()) {
            throw new IOException("WebDAV URL is not configured");
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(WebDavUtils.buildTargetUrl(baseUrl, fileName)).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        WebDavUtils.applyBasicAuth(connection, configManager.getUsername(), configManager.getPassword());
        return connection;
    }

    private static void requireSuccess(HttpURLConnection connection) throws IOException {
        int code = connection.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("Server responded with HTTP " + code);
        }
    }

    private static void runAsync(IoAction action, String errorPrefix, SyncCallback callback) {
        EXECUTOR.execute(() -> {
            String message;
            try {
                message = action.run();
            } catch (Exception e) {
                callback.onError(errorPrefix + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
                return;
            }
            callback.onSuccess(message);
        });
    }
}
