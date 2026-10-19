package com.yurii.pavlenko.myassistant.backup;

import android.util.Base64;

import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

/** URL formatting and authentication helpers for WebDAV requests. */
public final class WebDavUtils {

    private WebDavUtils() {
    }

    /** Credentials and backups are sent only over an encrypted connection. */
    public static boolean isSecureUrl(String url) {
        return url != null && url.trim().regionMatches(true, 0, "https://", 0, "https://".length());
    }

    /** Appends a backup file name to the base WebDAV folder URL. */
    public static String buildTargetUrl(String baseUrl, String fileName) {
        String trimmed = baseUrl.trim();
        String separator = trimmed.endsWith("/") ? "" : "/";
        return trimmed + separator + fileName;
    }

    /** Applies HTTP Basic Authentication when credentials are provided. */
    public static void applyBasicAuth(HttpURLConnection connection, String username, String password) {
        if (username != null && !username.isEmpty() && password != null) {
            byte[] credentials = (username + ":" + password).getBytes(StandardCharsets.UTF_8);
            connection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString(credentials, Base64.NO_WRAP));
        }
    }
}