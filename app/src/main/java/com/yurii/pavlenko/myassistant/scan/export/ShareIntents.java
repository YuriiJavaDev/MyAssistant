package com.yurii.pavlenko.myassistant.scan.export;

import android.content.Context;
import android.content.Intent;

import com.yurii.pavlenko.myassistant.scan.files.ScanCache;

import java.io.File;

/** Builds the system intents that hand an exported file to other apps. */
public final class ShareIntents {

    private ShareIntents() {
    }

    public static Intent send(Context context, File file, ExportFormat format, CharSequence chooserTitle) {
        Intent send = new Intent(Intent.ACTION_SEND)
                .setType(format.getMimeType())
                .putExtra(Intent.EXTRA_STREAM, ScanCache.uriFor(context, file))
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return Intent.createChooser(send, chooserTitle);
    }

    public static Intent view(Context context, File file, ExportFormat format) {
        return new Intent(Intent.ACTION_VIEW)
                .setDataAndType(ScanCache.uriFor(context, file), format.getMimeType())
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }
}
