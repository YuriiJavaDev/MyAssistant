package com.yurii.pavlenko.myassistant.scan.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.yurii.pavlenko.myassistant.scan.model.ScanDocument;
import com.yurii.pavlenko.myassistant.scan.model.ScanPage;
import com.yurii.pavlenko.myassistant.scan.repository.ScanRepository;

import java.util.List;

/** State of the screen that shows the pages of one document. */
public class ScanDocumentViewModel extends AndroidViewModel {

    private final ScanRepository repository;
    private final long documentId;
    private ScanPage pendingSavePage;

    private ScanDocumentViewModel(@NonNull Application application, long documentId) {
        super(application);
        this.repository = new ScanRepository(application);
        this.documentId = documentId;
    }

    public LiveData<ScanDocument> getDocument() {
        return repository.getDocument(documentId);
    }

    public LiveData<List<ScanPage>> getPages() {
        return repository.observePages(documentId);
    }

    public void deletePage(ScanPage page) {
        repository.deletePage(page);
    }

    public void movePage(List<ScanPage> pages, int index, int direction) {
        repository.movePage(pages, index, direction);
    }

    /** Remembers which page the system file dialog is about to save, since the dialog returns only a location. */
    public void setPendingSavePage(ScanPage page) {
        pendingSavePage = page;
    }

    public ScanPage takePendingSavePage() {
        ScanPage page = pendingSavePage;
        pendingSavePage = null;
        return page;
    }

    public static final class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final long documentId;

        public Factory(Application application, long documentId) {
            this.application = application;
            this.documentId = documentId;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ScanDocumentViewModel(application, documentId);
        }
    }
}
