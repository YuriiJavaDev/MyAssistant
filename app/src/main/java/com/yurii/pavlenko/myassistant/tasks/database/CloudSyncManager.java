package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Uploads the local database to a WebDAV server and restores it from there. */
public class CloudSyncManager {

    private static final int TIMEOUT_MS = 10_000;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public interface SyncCallback {
        void onSuccess(String message);

        void onError(String error);
    }

    private interface IoAction {
        void run() throws IOException;
    }

    private final Context context;
    private final CloudConfigManager configManager;

    public CloudSyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.configManager = new CloudConfigManager(this.context);
    }

    public void uploadDatabase(SyncCallback callback) {
        runAsync(this::uploadDatabaseBlocking, "Database successfully uploaded to cloud", "Upload error: ", callback);
    }

    /**
     * Restores the database from the cloud. The file is downloaded and verified completely
     * before the local database is replaced, so a failed download leaves local data untouched.
     */
    public void downloadDatabase(SyncCallback callback) {
        runAsync(this::downloadDatabaseBlocking, "Database successfully restored from cloud", "Download error: ", callback);
    }

    /** Blocking upload for background workers that already run off the main thread. */
    public void uploadDatabaseBlocking() throws IOException {
        File dbFile = DatabaseFileManager.getDatabaseFile(context);
        if (!dbFile.exists()) {
            throw new IOException("Local database file not found");
        }

        HttpURLConnection connection = openConnection("PUT");
        try {
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode(dbFile.length());
            try (InputStream in = new FileInputStream(dbFile);
                 OutputStream out = connection.getOutputStream()) {
                DatabaseFileManager.copy(in, out);
            }
            requireSuccess(connection);
        } finally {
            connection.disconnect();
        }
    }

    private void downloadDatabaseBlocking() throws IOException {
        HttpURLConnection connection = openConnection("GET");
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

    private HttpURLConnection openConnection(String method) throws IOException {
        String baseUrl = configManager.getUrl();
        if (baseUrl.isEmpty()) {
            throw new IOException("WebDAV URL is not configured");
        }
        HttpURLConnection connection = (HttpURLConnection) new URL(WebDavUtils.buildTargetUrl(baseUrl)).openConnection();
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

    private static void runAsync(IoAction action, String successMessage, String errorPrefix, SyncCallback callback) {
        EXECUTOR.execute(() -> {
            try {
                action.run();
            } catch (Exception e) {
                callback.onError(errorPrefix + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
                return;
            }
            callback.onSuccess(successMessage);
        });
    }
}