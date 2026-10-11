package com.yurii.pavlenko.myassistant.scan.export;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;

import com.yurii.pavlenko.myassistant.scan.model.ScanItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/** Writes scans as pages of one PDF, each page shaped like the scan's paper format. */
final class PdfWriter {

    /** About 200 dpi on A4: sharp enough to read and print, small enough for an e-mail attachment. */
    private static final int MAX_LONG_SIDE = 2339;

    private PdfWriter() {
    }

    static void write(List<ScanItem> items, File target) throws IOException {
        PdfDocument document = new PdfDocument();
        Paint smoothing = new Paint(Paint.FILTER_BITMAP_FLAG);
        try {
            for (int i = 0; i < items.size(); i++) {
                addPage(document, items.get(i), i + 1, smoothing);
            }
            try (FileOutputStream out = new FileOutputStream(target)) {
                document.writeTo(out);
            }
        } finally {
            document.close();
        }
    }

    private static void addPage(PdfDocument document, ScanItem item, int pageNumber, Paint smoothing) throws IOException {
        Bitmap bitmap = decodeForPdf(item);
        try {
            int[] page = item.resolvePaperFormat().pdfPageSize(bitmap.getWidth(), bitmap.getHeight());
            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(page[0], page[1], pageNumber).create();
            PdfDocument.Page pdfPage = document.startPage(info);
            pdfPage.getCanvas().drawBitmap(bitmap, null, new Rect(0, 0, page[0], page[1]), smoothing);
            document.finishPage(pdfPage);
        } finally {
            bitmap.recycle();
        }
    }

    private static Bitmap decodeForPdf(ScanItem item) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeFile(item.getImagePath());
        if (bitmap == null) {
            throw new IOException("Scan file is missing: " + item.getImagePath());
        }
        int longSide = Math.max(bitmap.getWidth(), bitmap.getHeight());
        if (longSide <= MAX_LONG_SIDE) {
            return bitmap;
        }
        float scale = (float) MAX_LONG_SIDE / longSide;
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, Math.round(bitmap.getWidth() * scale), Math.round(bitmap.getHeight() * scale), true);
        bitmap.recycle();
        return scaled;
    }
}
