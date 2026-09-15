package com.yurii.pavlenko.myassistant.tasks.database;

import android.util.Base64;

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
     *
     * @param urlStr   WebDAV server URL
     * @param username Username or email
     * @param password Password or token
     * @param callback Callback to handle success or error on the main thread
     */
    public void testConnection(String urlStr, String username, String password, TestCallback callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                // Step 1: Validate and open URL connection using OPTIONS method
                URL url = new URL(urlStr);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("OPTIONS");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                // Step 2: Apply Basic Authentication if provided
                if (username != null && !username.isEmpty() && password != null) {
                    String credentials = username + ":" + password;
                    String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                    connection.setRequestProperty("Authorization", auth);
                }

                // Step 3: Connect and analyze response code
                connection.connect();
                int responseCode = connection.getResponseCode();

                // Check HTTP response codes (200-299 indicates success)
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