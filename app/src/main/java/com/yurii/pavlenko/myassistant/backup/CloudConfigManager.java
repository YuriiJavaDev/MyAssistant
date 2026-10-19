package com.yurii.pavlenko.myassistant.backup;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CloudConfigManager {

    private static final String PREFS_NAME = "myassistant_secure_cloud_prefs";
    private static final String KEY_URL = "webdav_url";
    private static final String KEY_USERNAME = "webdav_username";
    private static final String KEY_PASSWORD = "webdav_password";
    private static final String KEY_AUTO_BACKUP = "is_auto_backup_enabled";
    private static final String KEY_SCAN_SIGNATURE = "last_uploaded_scan_signature";
    private static final String KEY_FLIGHT_SIGNATURE = "last_uploaded_flight_signature";
    private static final String KEY_FLIGHT_REPORTS = "uploaded_flight_reports";

    private final SharedPreferences sharedPreferences;

    /**
     * Opens the encrypted store. When the keystore cannot be used the store is reset once, since an unreadable
     * one cannot be recovered; if that fails too the manager stays unavailable instead of keeping the
     * credentials unencrypted.
     */
    public CloudConfigManager(Context context) {
        SharedPreferences prefs = openEncrypted(context);
        if (prefs == null) {
            context.deleteSharedPreferences(PREFS_NAME);
            prefs = openEncrypted(context);
        }
        this.sharedPreferences = prefs;
    }

    private static SharedPreferences openEncrypted(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException | RuntimeException e) {
            return null;
        }
    }

    /** False when the secure storage cannot be opened, in which case nothing is stored or read. */
    public boolean isAvailable() {
        return sharedPreferences != null;
    }

    /** Returns false when the settings could not be stored. */
    public boolean saveConfig(String url, String username, String password, boolean autoBackup) {
        if (!isAvailable()) {
            return false;
        }
        sharedPreferences.edit()
                .putString(KEY_URL, url != null ? url.trim() : "")
                .putString(KEY_USERNAME, username != null ? username.trim() : "")
                .putString(KEY_PASSWORD, password != null ? password : "")
                .putBoolean(KEY_AUTO_BACKUP, autoBackup)
                .apply();
        return true;
    }

    public String getUrl() {
        return getString(KEY_URL);
    }

    public String getUsername() {
        return getString(KEY_USERNAME);
    }

    public String getPassword() {
        return getString(KEY_PASSWORD);
    }

    public boolean isAutoBackupEnabled() {
        return isAvailable() && sharedPreferences.getBoolean(KEY_AUTO_BACKUP, false);
    }

    /** Identifies the scans as they were at the last upload, so unchanged scans are not uploaded again. */
    public String getUploadedScanSignature() {
        return getString(KEY_SCAN_SIGNATURE);
    }

    public void saveUploadedScanSignature(String signature) {
        putString(KEY_SCAN_SIGNATURE, signature);
    }

    /** Identifies the flight database as it was at the last upload, so an unchanged one is not uploaded again. */
    public String getUploadedFlightSignature() {
        return getString(KEY_FLIGHT_SIGNATURE);
    }

    public void saveUploadedFlightSignature(String signature) {
        putString(KEY_FLIGHT_SIGNATURE, signature);
    }

    /** Flight reports already in the cloud, each as the flight id and the size of the PDF that was sent. */
    public Set<String> getUploadedFlightReports() {
        if (!isAvailable()) {
            return new HashSet<>();
        }
        return new HashSet<>(sharedPreferences.getStringSet(KEY_FLIGHT_REPORTS, Collections.emptySet()));
    }

    public void saveUploadedFlightReports(Set<String> reports) {
        if (isAvailable()) {
            sharedPreferences.edit().putStringSet(KEY_FLIGHT_REPORTS, new HashSet<>(reports)).apply();
        }
    }

    private String getString(String key) {
        return isAvailable() ? sharedPreferences.getString(key, "") : "";
    }

    private void putString(String key, String value) {
        if (isAvailable()) {
            sharedPreferences.edit().putString(key, value).apply();
        }
    }
}
