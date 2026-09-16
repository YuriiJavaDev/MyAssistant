package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manager class to handle cloud synchronization (backup export and import) via WebDAV.
 */
public class CloudSyncManager {

    private final Context context;
    private final CloudConfigManager configManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public interface SyncCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public CloudSyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.configManager = new CloudConfigManager(this.context);
    }

    /**
     * Uploads the local SQLite database to the WebDAV server using HTTP PUT method.
     */
    public void uploadDatabase(SyncCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String urlStr = configManager.getUrl();
                if (urlStr == null || urlStr.isEmpty()) {
                    callback.onError("WebDAV URL is not configured");
                    return;
                }

                URL url = new URL(WebDavUtils.buildTargetUrl(urlStr));

                File dbFile = context.getDatabasePath("task_database");
                if (!dbFile.exists()) {
                    callback.onError("Local database file not found");
                    return;
                }

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                WebDavUtils.applyBasicAuth(connection, configManager.getUsername(), configManager.getPassword());

                try (OutputStream os = connection.getOutputStream();
                     FileInputStream fis = new FileInputStream(dbFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    os.flush();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    callback.onSuccess("Database successfully uploaded to cloud");
                } else {
                    callback.onError("Upload failed with HTTP code: " + responseCode);
                }

            } catch (Exception e) {
                callback.onError("Upload error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    /**
     * Downloads the database from the WebDAV server and safely replaces the local Room database.
     */
    public void downloadDatabase(SyncCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String urlStr = configManager.getUrl();
                if (urlStr == null || urlStr.isEmpty()) {
                    callback.onError("WebDAV URL is not configured");
                    return;
                }

                URL url = new URL(WebDavUtils.buildTargetUrl(urlStr));

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                WebDavUtils.applyBasicAuth(connection, configManager.getUsername(), configManager.getPassword());

                int responseCode = connection.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    callback.onError("Download failed with HTTP code: " + responseCode);
                    return;
                }

                try {
                    AppDatabase.getInstance(context).close();
                } catch (Exception ignored) {
                }

                File dbFile = context.getDatabasePath("task_database");
                if (dbFile.getParentFile() != null && !dbFile.getParentFile().exists()) {
                    dbFile.getParentFile().mkdirs();
                }

                try (InputStream is = connection.getInputStream();
                     OutputStream os = new FileOutputStream(dbFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    os.flush();
                }

                callback.onSuccess("Database successfully restored from cloud");

            } catch (Exception e) {
                callback.onError("Download error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
}