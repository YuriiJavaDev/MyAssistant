package com.yurii.pavlenko.myassistant.tasks.database;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helper class to test WebDAV server connectivity and credentials asynchronously.
 */
public class WebDavTestClient {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public interface TestCallback {
        void onSuccess();
        void onError(String error);
    }

    /**
     * Tests connection to the WebDAV server.
     */
    public void testConnection(String urlStr, String username, String password, TestCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlStr);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("OPTIONS");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                WebDavUtils.applyBasicAuth(connection, username, password);

                connection.connect();
                int responseCode = connection.getResponseCode();

                if (responseCode == 401 || responseCode == 403) {
                    callback.onError("Authentication failed: Check username and password");
                } else if (responseCode >= 200 && responseCode < 300) {
                    callback.onSuccess();
                } else {
                    callback.onError("Server responded with code: " + responseCode);
                }

            } catch (Exception e) {
                callback.onError("Connection error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
}