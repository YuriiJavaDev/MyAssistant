package com.yurii.pavlenko.myassistant.scan.image;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import com.yurii.pavlenko.myassistant.scan.imaging.PixelImage;

import java.io.IOException;
import java.io.InputStream;

/** Bridge between Android bitmaps and the platform-independent imaging package. */
public final class BitmapCodec {

    private BitmapCodec() {
    }

    /** Decodes a photo upright (EXIF rotation applied) with its longer side at most maxLongSide. */
    public static Bitmap decode(ContentResolver resolver, Uri uri, int maxLongSide) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = open(resolver, uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw new IOException("Not an image: " + uri);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSizeFor(Math.max(bounds.outWidth, bounds.outHeight), maxLongSide);
        Bitmap decoded;
        try (InputStream in = open(resolver, uri)) {
            decoded = BitmapFactory.decodeStream(in, null, options);
        }
        if (decoded == null) {
            throw new IOException("Cannot decode " + uri);
        }

        int rotation;
        try (InputStream in = open(resolver, uri)) {
            rotation = new ExifInterface(in).getRotationDegrees();
        }
        return fitAndRotate(decoded, rotation, maxLongSide);
    }

    public static PixelImage toPixelImage(Bitmap bitmap) {
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        return new PixelImage(bitmap.getWidth(), bitmap.getHeight(), pixels);
    }

    public static Bitmap toBitmap(PixelImage image) {
        return Bitmap.createBitmap(image.getPixels(), image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
    }

    private static InputStream open(ContentResolver resolver, Uri uri) throws IOException {
        InputStream in = resolver.openInputStream(uri);
        if (in == null) {
            throw new IOException("Cannot open " + uri);
        }
        return in;
    }

    /** Largest power of two that still leaves the image at least maxLongSide on its long side. */
    private static int sampleSizeFor(int longSide, int maxLongSide) {
        int sample = 1;
        while (longSide / (sample * 2) >= maxLongSide) {
            sample *= 2;
        }
        return sample;
    }

    private static Bitmap fitAndRotate(Bitmap bitmap, int rotationDegrees, int maxLongSide) {
        float scale = Math.min(1f, (float) maxLongSide / Math.max(bitmap.getWidth(), bitmap.getHeight()));
        if (scale == 1f && rotationDegrees == 0) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        matrix.postRotate(rotationDegrees);
        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (result != bitmap) {
            bitmap.recycle();
        }
        return result;
    }
}
