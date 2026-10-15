package com.yurii.pavlenko.myassistant.flights.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.flights.logic.FlightReportName;
import com.yurii.pavlenko.myassistant.flights.model.FlightRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/** Hands a flight report to other apps, which read it through the FileProvider. */
final class FlightReportFiles {

    private static final String FOLDER = "flight_reports";
    private static final String PDF_MIME = "application/pdf";

    private FlightReportFiles() {
    }

    static void view(Context context, FlightRecord flight, byte[] pdf) {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(uriOf(context, flight, pdf), PDF_MIME)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        start(context, intent);
    }

    static void share(Context context, FlightRecord flight, byte[] pdf) {
        Intent intent = new Intent(Intent.ACTION_SEND)
                .setType(PDF_MIME)
                .putExtra(Intent.EXTRA_STREAM, uriOf(context, flight, pdf))
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        start(context, Intent.createChooser(intent, context.getString(R.string.flights_report_share)));
    }

    private static void start(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException noViewer) {
            Toast.makeText(context, R.string.flights_report_no_viewer, Toast.LENGTH_LONG).show();
        }
    }

    private static Uri uriOf(Context context, FlightRecord flight, byte[] pdf) {
        File folder = new File(context.getCacheDir(), FOLDER);
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new IllegalStateException("Cannot create the report folder");
        }
        File file = new File(folder, FlightReportName.of(flight.getStartMillis()));
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(pdf);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write the report", e);
        }
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }
}
