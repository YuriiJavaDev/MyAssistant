package com.yurii.pavlenko.myassistant.scan.export;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.yurii.pavlenko.myassistant.scan.files.FileCopier;
import com.yurii.pavlenko.myassistant.scan.files.ScanCache;
import com.yurii.pavlenko.myassistant.scan.model.ScanItem;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Prepares a scan as a PDF or JPEG file in the shareable cache, off the main thread. */
public class ScanExporter {

    public interface Callback {
        void onReady(File file);

        void onFailed(IOException error);
    }

    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ScanExporter(Context context) {
        this.context = context.getApplicationContext();
    }

    /** The callback is delivered on the main thread. */
    public void export(ScanItem item, ExportFormat format, Callback callback) {
        executor.execute(() -> {
            try {
                File file = createFile(item, format);
                mainHandler.post(() -> callback.onReady(file));
            } catch (IOException e) {
                mainHandler.post(() -> callback.onFailed(e));
            }
        });
    }

    /** Same as export, and additionally copies the file to a location the user picked in the system file dialog. */
    public void exportToUri(ScanItem item, ExportFormat format, Uri target, Callback callback) {
        executor.execute(() -> {
            try {
                File file = createFile(item, format);
                FileCopier.copy(context.getContentResolver(), file, target);
                mainHandler.post(() -> callback.onReady(file));
            } catch (IOException e) {
                mainHandler.post(() -> callback.onFailed(e));
            }
        });
    }

    public static String suggestedFileName(ScanItem item, ExportFormat format) {
        return safeFileName(item.getTitle()) + format.getExtension();
    }

    private File createFile(ScanItem item, ExportFormat format) throws IOException {
        File target = new File(ScanCache.freshShareDirectory(context), suggestedFileName(item, format));
        if (format == ExportFormat.PDF) {
            PdfWriter.write(Collections.singletonList(item), target);
        } else {
            FileCopier.copy(new File(item.getImagePath()), target);
        }
        return target;
    }

    /** Keeps letters, digits, dots, dashes and spaces; anything else would be rejected by some file systems. */
    private static String safeFileName(String title) {
        String cleaned = title.replaceAll("[^\\p{L}\\p{N}._ -]", "_").trim();
        return cleaned.isEmpty() ? "scan" : cleaned;
    }
}
