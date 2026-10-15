package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class CloudConfigManager {

    private static final String PREFS_NAME = "myassistant_secure_cloud_prefs";
    private static final String KEY_URL = "webdav_url";
    private static final String KEY_USERNAME = "webdav_username";
    private static final String KEY_PASSWORD = "webdav_password";
    private static final String KEY_AUTO_BACKUP = "is_auto_backup_enabled";
    private static final String KEY_SCAN_SIGNATURE = "last_uploaded_scan_signature";
    private static final String KEY_FLIGHT_SIGNATURE = "last_uploaded_flight_signature";

    private final SharedPreferences sharedPreferences;

    public CloudConfigManager(Context context) {
        SharedPreferences prefs;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            // A fallback option for rare encryption errors on older devices.
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        this.sharedPreferences = prefs;
    }

    public void saveConfig(String url, String username, String password, boolean autoBackup) {
        sharedPreferences.edit()
                .putString(KEY_URL, url != null ? url.trim() : "")
                .putString(KEY_USERNAME, username != null ? username.trim() : "")
                .putString(KEY_PASSWORD, password != null ? password : "")
                .putBoolean(KEY_AUTO_BACKUP, autoBackup)
                .apply();
    }

    public String getUrl() {
        return sharedPreferences.getString(KEY_URL, "");
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "");
    }

    public String getPassword() {
        return sharedPreferences.getString(KEY_PASSWORD, "");
    }

    public boolean isAutoBackupEnabled() {
        return sharedPreferences.getBoolean(KEY_AUTO_BACKUP, false);
    }

    /** Identifies the scans as they were at the last upload, so unchanged scans are not uploaded again. */
    public String getUploadedScanSignature() {
        return sharedPreferences.getString(KEY_SCAN_SIGNATURE, "");
    }

    public void saveUploadedScanSignature(String signature) {
        sharedPreferences.edit().putString(KEY_SCAN_SIGNATURE, signature).apply();
    }

    /** Identifies the flight database as it was at the last upload, so an unchanged one is not uploaded again. */
    public String getUploadedFlightSignature() {
        return sharedPreferences.getString(KEY_FLIGHT_SIGNATURE, "");
    }

    public void saveUploadedFlightSignature(String signature) {
        sharedPreferences.edit().putString(KEY_FLIGHT_SIGNATURE, signature).apply();
    }
}
