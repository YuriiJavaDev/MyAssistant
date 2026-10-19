package com.yurii.pavlenko.myassistant.backup;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.yurii.pavlenko.myassistant.scan.backup.ScanBackupArchive;
import com.yurii.pavlenko.myassistant.scan.backup.ScanBackupRestorer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Exports the local data (task, step and flight databases and scan archive) to a folder the user picks or to another app,
 * and imports it back from user-selected files. All work runs off the main thread and reports back on it.
 */
public final class DatabaseBackupManager {

    private static final String TAG = "DatabaseBackupManager";
    private static final String CACHE_FOLDER = "backups";
    private static final String DATABASE_MIME = "application/octet-stream";
    private static final String ZIP_MIME = "application/zip";
    private static final byte[] SQLITE_HEADER = {'S', 'Q', 'L', 'i', 't', 'e', ' ', 'f', 'o', 'r', 'm', 'a', 't', ' ', '3', 0};
    private static final byte[] ZIP_HEADER = {'P', 'K'};
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private DatabaseBackupManager() {
    }

    /** Prepares a chooser intent that shares the backup files with another app; the callback gets null on failure. */
    public static void prepareShareIntent(Context context, Consumer<Intent> onReady) {
        Context appContext = context.getApplicationContext();
        run(() -> {
            deleteCachedBackups(appContext);
            List<File> files = new ArrayList<>();
            for (ManagedDatabase database : existingDatabases(appContext)) {
                files.add(copyDatabaseToCache(appContext, database));
            }
            if (ScanBackupArchive.hasData(appContext)) {
                files.add(writeScanArchiveToCache(appContext));
            }
            return createShareIntent(appContext, files);
        }, onReady);
    }

    /** Writes the backup files into a folder chosen with the system folder picker (device, SD card, USB drive, Drive...). */
    public static void exportToFolder(Context context, Uri folderUri, Consumer<Boolean> onDone) {
        Context appContext = context.getApplicationContext();
        run(() -> {
            DocumentFile folder = DocumentFile.fromTreeUri(appContext, folderUri);
            if (folder == null || !folder.canWrite()) {
                throw new IOException("Cannot write to the chosen folder");
            }
            for (ManagedDatabase database : existingDatabases(appContext)) {
                try (InputStream in = new FileInputStream(database.getFile(appContext))) {
                    writeToFolder(appContext, folder, database.getBackupFileName(), DATABASE_MIME, out -> DatabaseFileManager.copy(in, out));
                }
            }
            if (ScanBackupArchive.hasData(appContext)) {
                writeToFolder(appContext, folder, ScanBackupArchive.FILE_NAME, ZIP_MIME, out -> ScanBackupArchive.write(appContext, out));
            }
            return true;
        }, result -> onDone.accept(Boolean.TRUE.equals(result)));
    }

    /**
     * Restores from the selected files: task, step and flight databases and/or a scan archive, recognised by their content.
     * Each file is verified before it replaces local data, so a wrong or damaged file leaves it untouched.
     */
    public static void importFiles(Context context, List<Uri> uris, Consumer<Boolean> onDone) {
        Context appContext = context.getApplicationContext();
        run(() -> {
            int restored = 0;
            for (Uri uri : uris) {
                if (importFile(appContext, uri)) {
                    restored++;
                }
            }
            return restored > 0;
        }, result -> onDone.accept(Boolean.TRUE.equals(result)));
    }

    /** Folder in the cache that the FileProvider shares. */
    static File cacheFolder(Context context) throws IOException {
        File folder = new File(context.getCacheDir(), CACHE_FOLDER);
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new IOException("Cannot create the backup folder");
        }
        return folder;
    }

    /** Copies shared earlier are no longer needed once a new share starts, and they hold personal data. */
    private static void deleteCachedBackups(Context context) throws IOException {
        File[] old = cacheFolder(context).listFiles();
        if (old != null) {
            for (File file : old) {
                if (!file.delete()) {
                    Log.w(TAG, "Cannot delete " + file.getName());
                }
            }
        }
    }

    private interface Work<T> {
        T run() throws IOException;
    }

    private interface StreamWriter {
        void write(OutputStream out) throws IOException;
    }

    private static <T> void run(Work<T> work, Consumer<T> onDone) {
        EXECUTOR.execute(() -> {
            T result = null;
            try {
                result = work.run();
            } catch (IOException | IllegalArgumentException e) {
                Log.e(TAG, "Backup operation failed", e);
            }
            T finalResult = result;
            MAIN.post(() -> onDone.accept(finalResult));
        });
    }

    /** The databases that have been created on this phone; the task database must be among them. */
    private static List<ManagedDatabase> existingDatabases(Context context) throws IOException {
        List<ManagedDatabase> existing = new ArrayList<>();
        for (ManagedDatabase database : ManagedDatabase.values()) {
            if (database.getFile(context).exists()) {
                existing.add(database);
            }
        }
        if (!existing.contains(ManagedDatabase.TASKS)) {
            throw new IOException("Local database file not found");
        }
        return existing;
    }

    private static File copyDatabaseToCache(Context context, ManagedDatabase database) throws IOException {
        File target = new File(cacheFolder(context), database.getBackupFileName());
        try (InputStream in = new FileInputStream(database.getFile(context));
             OutputStream out = new FileOutputStream(target)) {
            DatabaseFileManager.copy(in, out);
        }
        return target;
    }

    private static File writeScanArchiveToCache(Context context) throws IOException {
        File target = new File(cacheFolder(context), ScanBackupArchive.FILE_NAME);
        try (OutputStream out = new FileOutputStream(target)) {
            ScanBackupArchive.write(context, out);
        }
        return target;
    }

    private static Intent createShareIntent(Context context, List<File> files) {
        ArrayList<Uri> uris = new ArrayList<>();
        for (File file : files) {
            uris.add(FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file));
        }
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return Intent.createChooser(intent, "Export Backup");
    }

    /** An existing file with the same name is replaced, so the folder always holds the latest backup. */
    private static void writeToFolder(Context context, DocumentFile folder, String name, String mime, StreamWriter writer) throws IOException {
        DocumentFile existing = folder.findFile(name);
        if (existing != null) {
            existing.delete();
        }
        DocumentFile target = folder.createFile(mime, name);
        if (target == null) {
            throw new IOException("Cannot create " + name);
        }
        try (OutputStream out = context.getContentResolver().openOutputStream(target.getUri())) {
            if (out == null) {
                throw new IOException("Cannot open " + name);
            }
            writer.write(out);
        }
    }

    /** Returns false for a file that is neither one of the app's databases nor a scan archive. */
    private static boolean importFile(Context context, Uri uri) throws IOException {
        try (InputStream raw = context.getContentResolver().openInputStream(uri)) {
            if (raw == null) {
                return false;
            }
            BufferedInputStream in = new BufferedInputStream(raw);
            if (startsWith(in, SQLITE_HEADER)) {
                DatabaseFileManager.Staged staged = DatabaseFileManager.stage(context, in);
                DatabaseFileManager.replaceWith(context, staged);
                return true;
            }
            if (startsWith(in, ZIP_HEADER)) {
                ScanBackupRestorer.restore(context, in);
                return true;
            }
            return false;
        }
    }

    /** Looks at the first bytes without consuming them. */
    private static boolean startsWith(BufferedInputStream in, byte[] header) throws IOException {
        in.mark(header.length);
        byte[] actual = new byte[header.length];
        int total = 0;
        int read;
        while (total < header.length && (read = in.read(actual, total, header.length - total)) != -1) {
            total += read;
        }
        in.reset();
        return total == header.length && Arrays.equals(actual, header);
    }
}
