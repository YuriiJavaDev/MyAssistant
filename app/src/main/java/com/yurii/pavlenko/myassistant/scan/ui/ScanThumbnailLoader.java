package com.yurii.pavlenko.myassistant.scan.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Loads list thumbnails in the background; a view that was recycled for another scan meanwhile is left alone. */
public class ScanThumbnailLoader {

    private static final int THUMBNAIL_SIDE = 240;
    private static final int CACHE_DIVISOR = 16;

    private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / CACHE_DIVISOR)) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void load(ImageView view, String path) {
        view.setTag(path);
        Bitmap cached = cache.get(path);
        if (cached != null) {
            view.setImageBitmap(cached);
            return;
        }
        view.setImageDrawable(null);
        executor.execute(() -> {
            Bitmap thumbnail = decode(path);
            if (thumbnail == null) {
                return;
            }
            cache.put(path, thumbnail);
            mainHandler.post(() -> {
                if (path.equals(view.getTag())) {
                    view.setImageBitmap(thumbnail);
                }
            });
        });
    }

    public void release() {
        executor.shutdownNow();
    }

    private static Bitmap decode(String path) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = Math.max(1, Math.max(bounds.outWidth, bounds.outHeight) / THUMBNAIL_SIDE);
        return BitmapFactory.decodeFile(path, options);
    }
}
