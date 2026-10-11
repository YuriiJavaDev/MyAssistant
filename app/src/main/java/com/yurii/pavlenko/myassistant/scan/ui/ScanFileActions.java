package com.yurii.pavlenko.myassistant.scan.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.scan.export.ExportFormat;
import com.yurii.pavlenko.myassistant.scan.export.ScanExporter;
import com.yurii.pavlenko.myassistant.scan.export.ShareIntents;
import com.yurii.pavlenko.myassistant.scan.model.ScanPage;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/** Export-then-hand-over flows (share, view, save to a chosen location) shared by the list and document screens. */
final class ScanFileActions {

    private final Context context;
    private final ScanExporter exporter;

    ScanFileActions(Context context) {
        this.context = context;
        this.exporter = new ScanExporter(context);
    }

    void share(String title, List<ScanPage> pages, ExportFormat format) {
        export(title, pages, format, files -> startActivitySafely(
                ShareIntents.send(context, files, format, context.getString(R.string.scan_share_title)),
                R.string.scan_error_no_app));
    }

    void view(String title, ScanPage page, ExportFormat format) {
        export(title, Collections.singletonList(page), format, files -> startActivitySafely(
                ShareIntents.view(context, files.get(0), format), R.string.scan_error_no_viewer));
    }

    void saveTo(String title, List<ScanPage> pages, ExportFormat format, Uri target) {
        exporter.exportToUri(title, pages, format, target, new ScanExporter.Callback() {
            @Override
            public void onReady(List<File> files) {
                showMessage(R.string.scan_saved_to_device);
            }

            @Override
            public void onFailed(IOException error) {
                showMessage(R.string.scan_error_export);
            }
        });
    }

    private void export(String title, List<ScanPage> pages, ExportFormat format, Consumer<List<File>> onReady) {
        exporter.export(title, pages, format, new ScanExporter.Callback() {
            @Override
            public void onReady(List<File> files) {
                onReady.accept(files);
            }

            @Override
            public void onFailed(IOException error) {
                showMessage(R.string.scan_error_export);
            }
        });
    }

    private void startActivitySafely(Intent intent, int missingAppMessage) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showMessage(missingAppMessage);
        }
    }

    private void showMessage(int message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
