package com.yurii.pavlenko.myassistant.flights.report;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** Encodes pixels as JPEG with the platform encoder. */
final class BitmapJpegEncoder implements PdfImageCompressor.JpegEncoder {

    private static final int QUALITY = 85;

    @Override
    public byte[] encode(int width, int height, byte[] rgb) throws IOException {
        int[] pixels = new int[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = 0xFF000000 | (rgb[3 * i] & 0xFF) << 16 | (rgb[3 * i + 1] & 0xFF) << 8 | (rgb[3 * i + 2] & 0xFF);
        }
        Bitmap bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, out)) {
                throw new IOException("Cannot encode the image");
            }
            return out.toByteArray();
        } finally {
            bitmap.recycle();
        }
    }
}
