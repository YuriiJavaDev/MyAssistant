package com.yurii.pavlenko.myassistant.scan.export;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.yurii.pavlenko.myassistant.scan.files.FileCopier;
import com.yurii.pavlenko.myassistant.scan.files.ScanCache;
import com.yurii.pavlenko.myassistant.scan.model.ScanPage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Prepares pages as files in the shareable cache, off the main thread: one PDF with all pages,
 * or one JPEG per page (named title_1.jpg, title_2.jpg ... when there are several).
 */
public class ScanExporter {

    public interface Callback {
        void onReady(List<File> files);

        void onFailed(IOException error);
    }

    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ScanExporter(Context context) {
        this.context = context.getApplicationContext();
    }

    /** The callback is delivered on the main thread. */
    public void export(String title, List<ScanPage> pages, ExportFormat format, Callback callback) {
        executor.execute(() -> {
            try {
                List<File> files = createFiles(title, pages, format);
                mainHandler.post(() -> callback.onReady(files));
            } catch (IOException e) {
                mainHandler.post(() -> callback.onFailed(e));
            }
        });
    }

    /** Same as export, and additionally copies the first resulting file to a location picked in the system file dialog. */
    public void exportToUri(String title, List<ScanPage> pages, ExportFormat format, Uri target, Callback callback) {
        executor.execute(() -> {
            try {
                List<File> files = createFiles(title, pages, format);
                FileCopier.copy(context.getContentResolver(), files.get(0), target);
                mainHandler.post(() -> callback.onReady(files));
            } catch (IOException e) {
                mainHandler.post(() -> callback.onFailed(e));
            }
        });
    }

    public static String suggestedFileName(String title, ExportFormat format) {
        return safeFileName(title) + format.getExtension();
    }

    private List<File> createFiles(String title, List<ScanPage> pages, ExportFormat format) throws IOException {
        File directory = ScanCache.freshShareDirectory(context);
        if (format == ExportFormat.PDF) {
            File pdf = new File(directory, suggestedFileName(title, format));
            PdfWriter.write(pages, pdf);
            return Collections.singletonList(pdf);
        }
        List<File> images = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            String suffix = pages.size() == 1 ? "" : "_" + (i + 1);
            File image = new File(directory, safeFileName(title) + suffix + format.getExtension());
            FileCopier.copy(new File(pages.get(i).getImagePath()), image);
            images.add(image);
        }
        return images;
    }

    /** Keeps letters, digits, dots, dashes and spaces; anything else would be rejected by some file systems. */
    private static String safeFileName(String title) {
        String cleaned = title.replaceAll("[^\\p{L}\\p{N}._ -]", "_").trim();
        return cleaned.isEmpty() ? "scan" : cleaned;
    }
}
