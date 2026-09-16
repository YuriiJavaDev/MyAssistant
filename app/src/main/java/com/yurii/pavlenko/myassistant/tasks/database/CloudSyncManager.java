package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;
import android.util.Base64;

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
     *
     * @param callback Callback to handle success or error messages
     */
    public void uploadDatabase(SyncCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                // Step 1: Retrieve cloud configuration securely via CloudConfigManager
                String urlStr = configManager.getUrl();
                String username = configManager.getUsername();
                String password = configManager.getPassword();

                if (urlStr == null || urlStr.isEmpty()) {
                    callback.onError("WebDAV URL is not configured");
                    return;
                }

                // Ensure URL correctly points to the target database backup file
                String targetUrlStr = urlStr.endsWith("/") ? urlStr + "myassistant_backup.db" : urlStr + "/myassistant_backup.db";
                URL url = new URL(targetUrlStr);

                // Step 2: Access local database file
                File dbFile = context.getDatabasePath("task_database");
                if (!dbFile.exists()) {
                    callback.onError("Local database file not found");
                    return;
                }

                // Step 3: Open HTTP connection and set PUT method
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                // Step 4: Apply Basic Authentication credentials if provided
                if (username != null && !username.isEmpty() && password != null) {
                    String credentials = username + ":" + password;
                    String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                    connection.setRequestProperty("Authorization", auth);
                }

                // Step 5: Stream local database bytes to the server output stream
                try (OutputStream os = connection.getOutputStream();
                     FileInputStream fis = new FileInputStream(dbFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    os.flush();
                }

                // Step 6: Verify HTTP response code (200-299 indicates success)
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
     *
     * @param callback Callback to handle success or error messages
     */
    public void downloadDatabase(SyncCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                // Step 1: Retrieve cloud configuration securely via CloudConfigManager
                String urlStr = configManager.getUrl();
                String username = configManager.getUsername();
                String password = configManager.getPassword();

                if (urlStr == null || urlStr.isEmpty()) {
                    callback.onError("WebDAV URL is not configured");
                    return;
                }

                // Ensure URL correctly points to the target database backup file
                String targetUrlStr = urlStr.endsWith("/") ? urlStr + "myassistant_backup.db" : urlStr + "/myassistant_backup.db";
                URL url = new URL(targetUrlStr);

                // Step 2: Open HTTP connection and set GET method
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                // Step 3: Apply Basic Authentication credentials if provided
                if (username != null && !username.isEmpty() && password != null) {
                    String credentials = username + ":" + password;
                    String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                    connection.setRequestProperty("Authorization", auth);
                }

                // Step 4: Verify HTTP response code
                int responseCode = connection.getResponseCode();
                if (responseCode < 200 || responseCode >= 300) {
                    callback.onError("Download failed with HTTP code: " + responseCode);
                    return;
                }

                // Step 5: Safely close active Room database instance before replacing file
                try {
                    AppDatabase.getInstance(context).close();
                } catch (Exception ignored) {
                }

                // Step 6: Stream downloaded data into local database file path
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