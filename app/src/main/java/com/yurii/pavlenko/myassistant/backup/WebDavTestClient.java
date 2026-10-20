package com.yurii.pavlenko.myassistant.backup;

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
        void onError(BackupFailure failure);
    }

    /**
     * Tests connection to the WebDAV server.
     */
    public void testConnection(String urlStr, String username, String password, TestCallback callback) {
        if (!WebDavUtils.isSecureUrl(urlStr)) {
            callback.onError(BackupFailure.of(BackupFailure.Kind.INSECURE_URL));
            return;
        }
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
                    callback.onError(BackupFailure.of(BackupFailure.Kind.AUTHENTICATION));
                } else if (responseCode >= 200 && responseCode < 300) {
                    callback.onSuccess();
                } else {
                    callback.onError(BackupFailure.serverCode(responseCode));
                }

            } catch (Exception e) {
                callback.onError(BackupFailure.withDetail(BackupFailure.Kind.CONNECTION, e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
}