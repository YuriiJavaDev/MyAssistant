package com.yurii.pavlenko.myassistant.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Minimal blocking GET that returns the response body as text; it must not be called on the main thread. */
public final class HttpText {

    private static final int TIMEOUT_MS = 10_000;
    private static final int MAX_BODY_BYTES = 5 * 1024 * 1024;
    /** Public servers, such as OpenStreetMap's, require a User-Agent that identifies the app. */
    private static final String USER_AGENT = "MyAssistant/1.0 (https://github.com/YuriiJavaDev/MyAssistant)";

    private HttpText() {
    }

    public static String get(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        try {
            int code = connection.getResponseCode();
            if (code < 200 || code >= 300) {
                throw new IOException("Server responded with HTTP " + code);
            }
            return readAll(connection.getInputStream(), MAX_BODY_BYTES);
        } finally {
            connection.disconnect();
        }
    }

    /** Reads the whole stream as UTF-8 text, failing when it is longer than {@code maxBytes}. */
    static String readAll(InputStream in, int maxBytes) throws IOException {
        try (InputStream stream = in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                if (out.size() + read > maxBytes) {
                    throw new IOException("Response is larger than " + maxBytes + " bytes");
                }
                out.write(buffer, 0, read);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }
}
