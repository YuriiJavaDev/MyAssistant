package com.yurii.pavlenko.myassistant.scan.files;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Copies files, either to another local file or to a location the user picked through the system file dialog. */
public final class FileCopier {

    private FileCopier() {
    }

    public static void copy(ContentResolver resolver, File source, Uri target) throws IOException {
        try (OutputStream out = resolver.openOutputStream(target)) {
            if (out == null) {
                throw new IOException("Cannot open " + target);
            }
            copyStream(source, out);
        }
    }

    public static void copy(File source, File target) throws IOException {
        try (OutputStream out = new FileOutputStream(target)) {
            copyStream(source, out);
        }
    }

    private static void copyStream(File source, OutputStream out) throws IOException {
        try (InputStream in = new FileInputStream(source)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
}
