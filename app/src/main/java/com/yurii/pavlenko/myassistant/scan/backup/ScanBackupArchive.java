package com.yurii.pavlenko.myassistant.scan.backup;

import android.content.Context;

import com.yurii.pavlenko.myassistant.scan.database.ScanDatabase;
import com.yurii.pavlenko.myassistant.scan.files.ScanFileStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The backup of all scans as one ZIP: the scan database plus every page image.
 * The database holds only the list of documents and pages, so it is useless without the images.
 */
public final class ScanBackupArchive {

    public static final String FILE_NAME = "myassistant_scans_backup.zip";

    static final String DATABASE_ENTRY = "scan_database";
    static final String IMAGES_PREFIX = "scans/";

    private ScanBackupArchive() {
    }

    public static boolean hasData(Context context) {
        return ScanDatabase.getDatabaseFile(context).exists();
    }

    /** Changes whenever scans are added, edited or deleted, so unchanged data need not be uploaded again. */
    public static String signature(Context context) {
        File database = ScanDatabase.getDatabaseFile(context);
        File[] images = new ScanFileStore(context).getDirectory().listFiles();
        return database.lastModified() + ":" + database.length() + ":" + (images == null ? 0 : images.length);
    }

    public static void write(Context context, OutputStream target) throws IOException {
        File database = ScanDatabase.getDatabaseFile(context);
        if (!database.exists()) {
            throw new IOException("There are no scans to back up");
        }
        ZipOutputStream zip = new ZipOutputStream(target);
        // Page images are JPEG already, compressing them again only costs time
        zip.setLevel(Deflater.NO_COMPRESSION);
        addFile(zip, DATABASE_ENTRY, database);

        File[] images = new ScanFileStore(context).getDirectory().listFiles();
        if (images != null) {
            for (File image : images) {
                addFile(zip, IMAGES_PREFIX + image.getName(), image);
            }
        }
        zip.finish();
    }

    private static void addFile(ZipOutputStream zip, String entryName, File file) throws IOException {
        zip.putNextEntry(new ZipEntry(entryName));
        try (InputStream in = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                zip.write(buffer, 0, read);
            }
        }
        zip.closeEntry();
    }
}
