package com.yurii.pavlenko.myassistant.scan.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.yurii.pavlenko.myassistant.scan.model.ScanDocument;
import com.yurii.pavlenko.myassistant.scan.model.ScanDocumentSummary;
import com.yurii.pavlenko.myassistant.scan.model.ScanPage;
import com.yurii.pavlenko.myassistant.scan.repository.ScanRepository;

import java.util.List;
import java.util.function.Consumer;

/** State of the scan list screen. */
public class ScanViewModel extends AndroidViewModel {

    private final ScanRepository repository;
    private ScanDocument pendingSave;

    public ScanViewModel(@NonNull Application application) {
        super(application);
        repository = new ScanRepository(application);
    }

    public LiveData<List<ScanDocumentSummary>> getDocuments() {
        return repository.getDocuments();
    }

    /** The callback runs on a background thread. */
    public void loadPages(ScanDocument document, Consumer<List<ScanPage>> onLoaded) {
        repository.loadPages(document.getId(), onLoaded);
    }

    public void rename(ScanDocument document, String title) {
        repository.renameDocument(document.getId(), title);
    }

    public void delete(ScanDocument document) {
        repository.deleteDocument(document);
    }

    /** Remembers which document the system file dialog is about to save, since the dialog returns only a location. */
    public void setPendingSave(ScanDocument document) {
        pendingSave = document;
    }

    public ScanDocument takePendingSave() {
        ScanDocument document = pendingSave;
        pendingSave = null;
        return document;
    }
}
