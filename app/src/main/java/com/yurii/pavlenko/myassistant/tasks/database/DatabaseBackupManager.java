package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class responsible for exporting and importing the Room database file.
 */
public class DatabaseBackupManager {

    private static final String DATABASE_NAME = "task_database";
    private static final String BACKUP_FILE_NAME = "myassistant_backup.db";

    /**
     * Prepares the backup file in cache and returns an Intent to share/save it anywhere (including Google Drive).
     */
    public static Intent getExportShareIntent(Context context) {
        try {
            File dbFile = context.getDatabasePath(DATABASE_NAME);
            if (!dbFile.exists()) {
                return null;
            }

            // Create a clean backup file in the app cache directory
            File cacheFolder = new File(context.getCacheDir(), "backups");
            if (!cacheFolder.exists()) {
                cacheFolder.mkdirs();
            }
            File backupFile = new File(cacheFolder, BACKUP_FILE_NAME);

            // Copy current database content to the export file
            try (InputStream fis = new FileInputStream(dbFile);
                 OutputStream fos = new FileOutputStream(backupFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.flush();
            }

            // Generate secure Uri via FileProvider
            Uri fileUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    backupFile
            );

            // Create share intent
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/octet-stream");
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            return Intent.createChooser(intent, "Export Database Backup");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Restores/imports the database from the selected source Uri over the existing local database.
     */
    public static boolean importDatabase(Context context, Uri uri) {
        try {
            try {
                AppDatabase.getInstance(context).close();
            } catch (Exception ignored) {}

            AppDatabase.clearInstance();

            File dbFile = context.getDatabasePath(DATABASE_NAME);
            File dbJournal = new File(dbFile.getPath() + "-journal");

            if (dbFile.exists()) dbFile.delete();
            if (dbJournal.exists()) dbJournal.delete();

            try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                 OutputStream outputStream = new FileOutputStream(dbFile)) {

                if (inputStream == null) return false;

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}