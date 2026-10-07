package com.yurii.pavlenko.myassistant.tasks.database;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Locates the local database file, verifies an incoming database before it is used,
 * and swaps it in. The live database is never touched until the incoming file is proven valid.
 */
public final class DatabaseFileManager {

    public static final String BACKUP_FILE_NAME = "myassistant_backup.db";

    private static final String STAGING_SUFFIX = ".incoming";
    private static final String[] SIDECAR_SUFFIXES = {"-journal", "-wal", "-shm"};
    private static final String TASKS_TABLE = "tasks";
    private static final int BUFFER_SIZE = 8192;

    private DatabaseFileManager() {
    }

    public static File getDatabaseFile(Context context) {
        return context.getDatabasePath(AppDatabase.DATABASE_NAME);
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * Fully copies an incoming database next to the live one and verifies it.
     *
     * @return the verified staging file, ready for {@link #replaceWith(Context, File)}
     * @throws IOException if the transfer fails or the data is not a valid MyAssistant database
     */
    public static File stage(Context context, InputStream source) throws IOException {
        File dbFile = getDatabaseFile(context);
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Cannot create the database directory");
        }

        File staged = new File(dbFile.getPath() + STAGING_SUFFIX);
        boolean valid = false;
        try {
            try (OutputStream out = new FileOutputStream(staged)) {
                copy(source, out);
            }
            valid = isValidTaskDatabase(staged);
        } finally {
            if (!valid) {
                staged.delete();
            }
        }
        if (!valid) {
            throw new IOException("The data is not a valid MyAssistant database");
        }
        return staged;
    }

    /** Closes the Room instance and puts the verified staging file in place of the live database. */
    public static void replaceWith(Context context, File staged) throws IOException {
        AppDatabase.closeInstance();

        File dbFile = getDatabaseFile(context);
        for (String suffix : SIDECAR_SUFFIXES) {
            new File(dbFile.getPath() + suffix).delete();
        }
        if (!staged.renameTo(dbFile)) {
            throw new IOException("Cannot replace the local database");
        }
    }

    private static boolean isValidTaskDatabase(File file) {
        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY)) {
            boolean intact = "ok".equalsIgnoreCase(DatabaseUtils.stringForQuery(db, "PRAGMA quick_check", null));
            // Throws SQLiteException when the tasks table does not exist
            return intact && DatabaseUtils.queryNumEntries(db, TASKS_TABLE) >= 0;
        } catch (SQLiteException e) {
            return false;
        }
    }
}