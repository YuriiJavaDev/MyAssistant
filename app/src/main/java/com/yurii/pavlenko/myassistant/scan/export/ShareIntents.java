package com.yurii.pavlenko.myassistant.scan.export;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.yurii.pavlenko.myassistant.scan.files.ScanCache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Builds the system intents that hand exported files to other apps. */
public final class ShareIntents {

    private ShareIntents() {
    }

    public static Intent send(Context context, List<File> files, ExportFormat format, CharSequence chooserTitle) {
        Intent send;
        if (files.size() == 1) {
            send = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, ScanCache.uriFor(context, files.get(0)));
        } else {
            ArrayList<Uri> uris = new ArrayList<>();
            for (File file : files) {
                uris.add(ScanCache.uriFor(context, file));
            }
            send = new Intent(Intent.ACTION_SEND_MULTIPLE).putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }
        send.setType(format.getMimeType()).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return Intent.createChooser(send, chooserTitle);
    }

    public static Intent view(Context context, File file, ExportFormat format) {
        return new Intent(Intent.ACTION_VIEW)
                .setDataAndType(ScanCache.uriFor(context, file), format.getMimeType())
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }
}
