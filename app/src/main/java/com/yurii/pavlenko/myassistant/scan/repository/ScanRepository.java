package com.yurii.pavlenko.myassistant.scan.repository;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;

import com.yurii.pavlenko.myassistant.scan.database.ScanDao;
import com.yurii.pavlenko.myassistant.scan.database.ScanDatabase;
import com.yurii.pavlenko.myassistant.scan.files.ScanFileStore;
import com.yurii.pavlenko.myassistant.scan.imaging.PaperFormat;
import com.yurii.pavlenko.myassistant.scan.model.ScanDocument;
import com.yurii.pavlenko.myassistant.scan.model.ScanDocumentSummary;
import com.yurii.pavlenko.myassistant.scan.model.ScanPage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** Keeps documents, their page rows and the page JPEG files consistent with each other. */
public class ScanRepository {

    /** Passed as the document id when a saved page should start a new document. */
    public static final long NEW_DOCUMENT = -1;

    private final ScanDatabase database;
    private final ScanDao scanDao;
    private final ScanFileStore fileStore;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ScanRepository(Application application) {
        database = ScanDatabase.getInstance(application);
        scanDao = database.scanDao();
        fileStore = new ScanFileStore(application);
    }

    public LiveData<List<ScanDocumentSummary>> getDocuments() {
        return scanDao.getDocuments();
    }

    public LiveData<ScanDocument> getDocument(long documentId) {
        return scanDao.getDocument(documentId);
    }

    public LiveData<List<ScanPage>> observePages(long documentId) {
        return scanDao.observePages(documentId);
    }

    /** Loads the pages in order; the callback runs on the repository's background thread. */
    public void loadPages(long documentId, Consumer<List<ScanPage>> onLoaded) {
        executor.execute(() -> onLoaded.accept(scanDao.getPages(documentId)));
    }

    /**
     * Stores the bitmap as the next page of the document, or as the first page of a new document.
     * The callbacks run on the repository's background thread.
     */
    public void savePage(long documentId, Bitmap bitmap, PaperFormat format, Consumer<Long> onSaved, Consumer<IOException> onFailed) {
        executor.execute(() -> {
            try {
                File file = fileStore.saveJpeg(bitmap);
                long targetDocument = documentId == NEW_DOCUMENT ? createDocument() : documentId;
                ScanPage page = new ScanPage(targetDocument, scanDao.nextPosition(targetDocument), file.getAbsolutePath(),
                        format.name(), bitmap.getWidth(), bitmap.getHeight());
                scanDao.insertPage(page);
                onSaved.accept(targetDocument);
            } catch (IOException e) {
                onFailed.accept(e);
            }
        });
    }

    public void renameDocument(long documentId, String title) {
        executor.execute(() -> scanDao.renameDocument(documentId, title));
    }

    public void deleteDocument(ScanDocument document) {
        executor.execute(() -> {
            List<ScanPage> pages = scanDao.getPages(document.getId());
            scanDao.deleteDocument(document);
            for (ScanPage page : pages) {
                fileStore.delete(page.getImagePath());
            }
        });
    }

    public void clearAll() {
        executor.execute(() -> {
            scanDao.deleteAllDocuments();
            fileStore.deleteAll();
        });
    }

    /** Removes the page; a document left without pages disappears with it. */
    public void deletePage(ScanPage page) {
        executor.execute(() -> {
            scanDao.deletePage(page);
            fileStore.delete(page.getImagePath());
            if (scanDao.countPages(page.getDocumentId()) == 0) {
                database.runInTransaction(() -> deleteEmptyDocument(page.getDocumentId()));
            }
        });
    }

    /** Swaps the page with its neighbour in the given direction (-1 up, +1 down). */
    public void movePage(List<ScanPage> pages, int index, int direction) {
        int target = index + direction;
        if (target < 0 || target >= pages.size()) {
            return;
        }
        ScanPage first = pages.get(index);
        ScanPage second = pages.get(target);
        executor.execute(() -> database.runInTransaction(() -> {
            scanDao.setPagePosition(first.getId(), second.getPosition());
            scanDao.setPagePosition(second.getId(), first.getPosition());
        }));
    }

    private long createDocument() {
        String title = "Scan " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        return scanDao.insertDocument(new ScanDocument(title, System.currentTimeMillis()));
    }

    private void deleteEmptyDocument(long documentId) {
        ScanDocument empty = new ScanDocument("", 0);
        empty.setId(documentId);
        scanDao.deleteDocument(empty);
    }
}
