package com.yurii.pavlenko.myassistant.scan.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.yurii.pavlenko.myassistant.scan.model.ScanItem;
import com.yurii.pavlenko.myassistant.scan.repository.ScanRepository;

import java.util.List;

/** State of the scan list screen. */
public class ScanViewModel extends AndroidViewModel {

    private final ScanRepository repository;
    private ScanItem pendingSave;

    public ScanViewModel(@NonNull Application application) {
        super(application);
        repository = new ScanRepository(application);
    }

    public LiveData<List<ScanItem>> getScans() {
        return repository.getAll();
    }

    public void rename(ScanItem item, String title) {
        repository.rename(item, title);
    }

    public void delete(ScanItem item) {
        repository.delete(item);
    }

    /** Remembers which scan the system file dialog is about to save, since the dialog returns only a location. */
    public void setPendingSave(ScanItem item) {
        pendingSave = item;
    }

    public ScanItem takePendingSave() {
        ScanItem item = pendingSave;
        pendingSave = null;
        return item;
    }
}
