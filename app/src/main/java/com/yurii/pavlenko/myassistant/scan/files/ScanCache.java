package com.yurii.pavlenko.myassistant.scan.files;

import android.content.Context;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;

/** Temporary files that other apps may read through the FileProvider: camera shots and files prepared for sharing. */
public final class ScanCache {

    private static final String CAPTURE_DIRECTORY = "scan_capture";
    private static final String SHARE_DIRECTORY = "scan_share";
    private static final String CAPTURE_FILE_NAME = "capture.jpg";

    private ScanCache() {
    }

    /** Fixed location, so a camera result can be matched even if the app process was recreated meanwhile. */
    public static File captureFile(Context context) {
        File directory = new File(context.getCacheDir(), CAPTURE_DIRECTORY);
        directory.mkdirs();
        return new File(directory, CAPTURE_FILE_NAME);
    }

    /** Empty directory for files about to be shared; leftovers of the previous share are removed. */
    public static File freshShareDirectory(Context context) {
        File directory = new File(context.getCacheDir(), SHARE_DIRECTORY);
        File[] leftovers = directory.listFiles();
        if (leftovers != null) {
            for (File leftover : leftovers) {
                leftover.delete();
            }
        }
        directory.mkdirs();
        return directory;
    }

    public static Uri uriFor(Context context, File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }
}
