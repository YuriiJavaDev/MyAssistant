package com.yurii.pavlenko.myassistant.tasks.database;

import android.util.Base64;
import java.net.HttpURLConnection;

/**
 * Utility helper for WebDAV operations, handling URL formatting and authentication headers.
 */
public class WebDavUtils {

    private static final String BACKUP_FILE_NAME = "myassistant_backup.db";

    /**
     * Formats the base WebDAV URL to correctly point to the backup database file.
     */
    public static String buildTargetUrl(String baseUrl) {
        if (baseUrl == null) return "";
        String trimmed = baseUrl.trim();
        return trimmed.endsWith("/") ? trimmed + BACKUP_FILE_NAME : trimmed + "/" + BACKUP_FILE_NAME;
    }

    /**
     * Applies HTTP Basic Authentication credentials if provided.
     */
    public static void applyBasicAuth(HttpURLConnection connection, String username, String password) {
        if (username != null && !username.isEmpty() && password != null) {
            String credentials = username + ":" + password;
            String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            connection.setRequestProperty("Authorization", auth);
        }
    }
}