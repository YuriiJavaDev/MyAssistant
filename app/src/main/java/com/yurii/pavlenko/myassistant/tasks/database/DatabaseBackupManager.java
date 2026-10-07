package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Exports the local database as a shareable file and imports it back from a user-selected file. */
public final class DatabaseBackupManager {

    private static final String TAG = "DatabaseBackupManager";
    private static final String CACHE_FOLDER = "backups";

    private DatabaseBackupManager() {
    }

    /** Returns a chooser intent that lets the user save or share a copy of the database, or null on failure. */
    public static Intent getExportShareIntent(Context context) {
        File dbFile = DatabaseFileManager.getDatabaseFile(context);
        if (!dbFile.exists()) {
            return null;
        }
        try {
            File backupFile = copyToCache(context, dbFile);
            Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", backupFile);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/octet-stream");
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            return Intent.createChooser(intent, "Export Database Backup");
        } catch (IOException | IllegalArgumentException e) {
            Log.e(TAG, "Database export failed", e);
            return null;
        }
    }

    /**
     * Replaces the local database with the selected file. The file is verified first,
     * so a wrong or damaged file leaves the current database untouched.
     */
    public static boolean importDatabase(Context context, Uri uri) {
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                return false;
            }
            File staged = DatabaseFileManager.stage(context, in);
            DatabaseFileManager.replaceWith(context, staged);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Database import failed", e);
            return false;
        }
    }

    private static File copyToCache(Context context, File dbFile) throws IOException {
        File folder = new File(context.getCacheDir(), CACHE_FOLDER);
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new IOException("Cannot create the backup folder");
        }
        File backupFile = new File(folder, DatabaseFileManager.BACKUP_FILE_NAME);
        try (InputStream in = new FileInputStream(dbFile);
             OutputStream out = new FileOutputStream(backupFile)) {
            DatabaseFileManager.copy(in, out);
        }
        return backupFile;
    }
}