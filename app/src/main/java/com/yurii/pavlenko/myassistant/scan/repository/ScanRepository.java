package com.yurii.pavlenko.myassistant.scan.repository;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;

import com.yurii.pavlenko.myassistant.scan.database.ScanDao;
import com.yurii.pavlenko.myassistant.scan.database.ScanDatabase;
import com.yurii.pavlenko.myassistant.scan.files.ScanFileStore;
import com.yurii.pavlenko.myassistant.scan.imaging.PaperFormat;
import com.yurii.pavlenko.myassistant.scan.model.ScanItem;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** Keeps a scan's JPEG file and its database row together. */
public class ScanRepository {

    private final ScanDao scanDao;
    private final ScanFileStore fileStore;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ScanRepository(Application application) {
        scanDao = ScanDatabase.getInstance(application).scanDao();
        fileStore = new ScanFileStore(application);
    }

    public LiveData<List<ScanItem>> getAll() {
        return scanDao.getAll();
    }

    /** Stores the bitmap and its row; the callbacks run on the repository's background thread. */
    public void save(Bitmap bitmap, String title, PaperFormat format, Consumer<ScanItem> onSaved, Consumer<IOException> onFailed) {
        executor.execute(() -> {
            try {
                File file = fileStore.saveJpeg(bitmap);
                ScanItem item = new ScanItem(title, file.getAbsolutePath(), format.name(),
                        bitmap.getWidth(), bitmap.getHeight(), System.currentTimeMillis());
                item.setId(scanDao.insert(item));
                onSaved.accept(item);
            } catch (IOException e) {
                onFailed.accept(e);
            }
        });
    }

    public void rename(ScanItem item, String title) {
        executor.execute(() -> scanDao.rename(item.getId(), title));
    }

    public void delete(ScanItem item) {
        executor.execute(() -> {
            scanDao.delete(item);
            fileStore.delete(item.getImagePath());
        });
    }
}
