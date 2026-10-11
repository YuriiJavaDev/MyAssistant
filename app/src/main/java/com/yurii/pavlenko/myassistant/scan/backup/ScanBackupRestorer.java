package com.yurii.pavlenko.myassistant.scan.backup;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.yurii.pavlenko.myassistant.scan.database.ScanDatabase;
import com.yurii.pavlenko.myassistant.scan.files.FileCopier;
import com.yurii.pavlenko.myassistant.scan.files.ScanFileStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Replaces all scans with the content of a backup ZIP. The archive is unpacked and verified in a staging
 * folder first, so a wrong or damaged file leaves the current scans untouched.
 */
public final class ScanBackupRestorer {

    private static final String STAGING_DIRECTORY = "scans_incoming";
    private static final String[] SIDECAR_SUFFIXES = {"-journal", "-wal", "-shm"};

    private ScanBackupRestorer() {
    }

    public static void restore(Context context, InputStream archive) throws IOException {
        File images = new ScanFileStore(context).getDirectory();
        File staging = new File(context.getFilesDir(), STAGING_DIRECTORY);
        deleteRecursively(staging);
        File stagedImages = new File(staging, "images");
        File stagedDatabase = new File(staging, ScanBackupArchive.DATABASE_ENTRY);
        try {
            if (!stagedImages.mkdirs()) {
                throw new IOException("Cannot create " + stagedImages);
            }
            unpack(archive, stagedDatabase, stagedImages);
            verifyAndRelocatePaths(stagedDatabase, images);
            install(context, stagedDatabase, stagedImages, images);
        } finally {
            deleteRecursively(staging);
        }
    }

    private static void unpack(InputStream archive, File database, File imagesDirectory) throws IOException {
        boolean foundDatabase = false;
        ZipInputStream zip = new ZipInputStream(archive);
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            String name = entry.getName();
            if (name.equals(ScanBackupArchive.DATABASE_ENTRY)) {
                extract(zip, database);
                foundDatabase = true;
            } else if (isImageEntry(name)) {
                extract(zip, new File(imagesDirectory, name.substring(ScanBackupArchive.IMAGES_PREFIX.length())));
            }
        }
        if (!foundDatabase) {
            throw new IOException("The file is not a MyAssistant scan backup");
        }
    }

    /** Only plain file names are accepted, so an entry can never write outside the staging folder. */
    private static boolean isImageEntry(String name) {
        if (!name.startsWith(ScanBackupArchive.IMAGES_PREFIX)) {
            return false;
        }
        String fileName = name.substring(ScanBackupArchive.IMAGES_PREFIX.length());
        return !fileName.isEmpty() && fileName.indexOf('/') < 0 && fileName.indexOf('\\') < 0 && !fileName.equals("..");
    }

    private static void extract(ZipInputStream zip, File target) throws IOException {
        try (OutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = zip.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    /** Checks the unpacked database and points its page records at the folder where the images will live. */
    private static void verifyAndRelocatePaths(File database, File imagesDirectory) throws IOException {
        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(database.getPath(), null, SQLiteDatabase.OPEN_READWRITE)) {
            boolean intact = "ok".equalsIgnoreCase(DatabaseUtils.stringForQuery(db, "PRAGMA quick_check", null));
            // Both throw SQLiteException when a table does not exist
            if (!intact || DatabaseUtils.queryNumEntries(db, "scan_documents") < 0 || DatabaseUtils.queryNumEntries(db, "scan_pages") < 0) {
                throw new IOException("The scan backup is damaged");
            }
            db.beginTransaction();
            try (Cursor pages = db.rawQuery("SELECT id, imagePath FROM scan_pages", null)) {
                while (pages.moveToNext()) {
                    String fileName = new File(pages.getString(1)).getName();
                    ContentValues values = new ContentValues();
                    values.put("imagePath", new File(imagesDirectory, fileName).getPath());
                    db.update("scan_pages", values, "id = ?", new String[]{String.valueOf(pages.getLong(0))});
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (SQLiteException e) {
            throw new IOException("The file is not a MyAssistant scan backup", e);
        }
    }

    private static void install(Context context, File stagedDatabase, File stagedImages, File images) throws IOException {
        ScanDatabase.closeInstance();

        File liveDatabase = ScanDatabase.getDatabaseFile(context);
        for (String suffix : SIDECAR_SUFFIXES) {
            new File(liveDatabase.getPath() + suffix).delete();
        }
        deleteRecursively(images);

        if (!stagedImages.renameTo(images)) {
            throw new IOException("Cannot replace the scan images");
        }
        File parent = liveDatabase.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        liveDatabase.delete();
        if (!stagedDatabase.renameTo(liveDatabase)) {
            FileCopier.copy(stagedDatabase, liveDatabase);
        }
    }

    private static void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }
}
