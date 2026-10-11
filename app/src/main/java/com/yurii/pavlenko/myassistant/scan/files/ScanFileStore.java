package com.yurii.pavlenko.myassistant.scan.files;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/** Permanent storage of finished scans as JPEG files inside the app's private directory. */
public class ScanFileStore {

    private static final String DIRECTORY = "scans";
    private static final int JPEG_QUALITY = 90;

    private final File directory;

    public ScanFileStore(Context context) {
        directory = new File(context.getFilesDir(), DIRECTORY);
    }

    public File saveJpeg(Bitmap bitmap) throws IOException {
        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IOException("Cannot create " + directory);
        }
        File file = new File(directory, "scan_" + UUID.randomUUID() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)) {
                throw new IOException("JPEG encoding failed");
            }
        }
        return file;
    }

    public void delete(String path) {
        File file = new File(path);
        if (file.exists() && !file.delete()) {
            file.deleteOnExit();
        }
    }
}
