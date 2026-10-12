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
 * Verifies an incoming database before it is used and swaps it in for the matching local one.
 * The live database is never touched until the incoming file is proven valid.
 */
public final class DatabaseFileManager {

    private static final String STAGING_FILE_NAME = "incoming.staged";
    private static final String[] SIDECAR_SUFFIXES = {"-journal", "-wal", "-shm"};
    private static final int BUFFER_SIZE = 8192;

    /** A verified incoming database and the local database it replaces. */
    public static final class Staged {
        private final File file;
        private final ManagedDatabase database;

        private Staged(File file, ManagedDatabase database) {
            this.file = file;
            this.database = database;
        }
    }

    private DatabaseFileManager() {
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * Fully copies an incoming database and verifies it; the data decides which database it is.
     *
     * @throws IOException if the transfer fails or the data is not a valid MyAssistant database
     */
    public static Staged stage(Context context, InputStream source) throws IOException {
        return stage(context, source, null);
    }

    /** Like {@link #stage(Context, InputStream)}, but the data must be the expected database. */
    public static Staged stage(Context context, InputStream source, ManagedDatabase expected) throws IOException {
        File parent = ManagedDatabase.TASKS.getFile(context).getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Cannot create the database directory");
        }

        File staged = new File(parent, STAGING_FILE_NAME);
        ManagedDatabase identified = null;
        try {
            try (OutputStream out = new FileOutputStream(staged)) {
                copy(source, out);
            }
            identified = identify(staged);
        } finally {
            if (identified == null || (expected != null && identified != expected)) {
                staged.delete();
            }
        }
        if (identified == null || (expected != null && identified != expected)) {
            throw new IOException("The data is not a valid MyAssistant database");
        }
        return new Staged(staged, identified);
    }

    /** Closes the Room instance and puts the verified staging file in place of the live database. */
    public static void replaceWith(Context context, Staged staged) throws IOException {
        staged.database.close();

        File dbFile = staged.database.getFile(context);
        for (String suffix : SIDECAR_SUFFIXES) {
            new File(dbFile.getPath() + suffix).delete();
        }
        if (!staged.file.renameTo(dbFile)) {
            throw new IOException("Cannot replace the local database");
        }
    }

    /** Returns null for a damaged file or one that holds none of the app's databases. */
    private static ManagedDatabase identify(File file) {
        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY)) {
            if (!"ok".equalsIgnoreCase(DatabaseUtils.stringForQuery(db, "PRAGMA quick_check", null))) {
                return null;
            }
            for (ManagedDatabase candidate : ManagedDatabase.values()) {
                if (hasTable(db, candidate.getRequiredTable())) {
                    return candidate;
                }
            }
            return null;
        } catch (SQLiteException e) {
            return null;
        }
    }

    private static boolean hasTable(SQLiteDatabase db, String table) {
        return DatabaseUtils.longForQuery(db,
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = ?", new String[]{table}) > 0;
    }
}
