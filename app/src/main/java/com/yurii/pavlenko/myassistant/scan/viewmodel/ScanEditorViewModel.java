package com.yurii.pavlenko.myassistant.scan.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.scan.image.BitmapCodec;
import com.yurii.pavlenko.myassistant.scan.imaging.DocumentDetector;
import com.yurii.pavlenko.myassistant.scan.imaging.PixelImage;
import com.yurii.pavlenko.myassistant.scan.imaging.Quad;
import com.yurii.pavlenko.myassistant.scan.imaging.ScanOptions;
import com.yurii.pavlenko.myassistant.scan.imaging.ScanPipeline;
import com.yurii.pavlenko.myassistant.scan.imaging.ScanResult;
import com.yurii.pavlenko.myassistant.scan.repository.ScanRepository;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

/** Holds one photo through the two editing steps: choosing the page corners, then adjusting the look of the scan. */
public class ScanEditorViewModel extends AndroidViewModel {

    public enum Stage {LOADING, CROP, ADJUST}

    private static final int SOURCE_MAX_SIDE = 2400;
    private static final int PREVIEW_MAX_SIDE = 1000;

    private final ScanRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicInteger latestPreviewRequest = new AtomicInteger();

    private final MutableLiveData<Stage> stage = new MutableLiveData<>(Stage.LOADING);
    private final MutableLiveData<Bitmap> sourceBitmap = new MutableLiveData<>();
    private final MutableLiveData<Quad> corners = new MutableLiveData<>();
    private final MutableLiveData<ScanOptions> options = new MutableLiveData<>(ScanOptions.defaults());
    private final MutableLiveData<Bitmap> preview = new MutableLiveData<>();
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saved = new MutableLiveData<>(false);

    private final AtomicBoolean saving = new AtomicBoolean();

    private PixelImage fullSource;
    private PixelImage previewSource;
    private boolean loadStarted;

    public ScanEditorViewModel(@NonNull Application application) {
        super(application);
        repository = new ScanRepository(application);
    }

    public LiveData<Stage> getStage() { return stage; }
    public LiveData<Bitmap> getSourceBitmap() { return sourceBitmap; }
    public LiveData<Quad> getCorners() { return corners; }
    public LiveData<ScanOptions> getOptions() { return options; }
    public LiveData<Bitmap> getPreview() { return preview; }
    public LiveData<Boolean> isBusy() { return busy; }
    public LiveData<Integer> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> isSaved() { return saved; }

    /** Loads the photo once; a recreated activity keeps the already loaded state. */
    public void load(Uri photo) {
        if (loadStarted) {
            return;
        }
        loadStarted = true;
        busy.setValue(true);
        executor.execute(() -> {
            try {
                Bitmap bitmap = BitmapCodec.decode(getApplication().getContentResolver(), photo, SOURCE_MAX_SIDE);
                fullSource = BitmapCodec.toPixelImage(bitmap);
                previewSource = fullSource.downscaledTo(PREVIEW_MAX_SIDE);
                corners.postValue(DocumentDetector.detect(previewSource));
                sourceBitmap.postValue(bitmap);
                stage.postValue(Stage.CROP);
            } catch (IOException | OutOfMemoryError e) {
                fail(R.string.scan_error_load);
            } finally {
                busy.postValue(false);
            }
        });
    }

    public void setCorners(Quad quad) {
        corners.setValue(quad);
    }

    public void detectCorners() {
        executor.execute(() -> corners.postValue(DocumentDetector.detect(previewSource)));
    }

    public void resetCorners() {
        corners.setValue(Quad.inset(0));
    }

    public void goToAdjust() {
        stage.setValue(Stage.ADJUST);
        requestPreview();
    }

    public void backToCrop() {
        stage.setValue(Stage.CROP);
    }

    public void updateOptions(UnaryOperator<ScanOptions> change) {
        options.setValue(change.apply(options.getValue()));
        requestPreview();
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    /** Renders the scan at full quality and stores it; a second tap while saving is ignored. */
    public void save() {
        if (!saving.compareAndSet(false, true)) {
            return;
        }
        Quad quad = corners.getValue();
        ScanOptions chosen = options.getValue();
        busy.setValue(true);
        executor.execute(() -> {
            try {
                ScanResult result = ScanPipeline.process(fullSource, quad, chosen, SOURCE_MAX_SIDE);
                repository.save(BitmapCodec.toBitmap(result.getImage()), defaultTitle(), result.getPaperFormat(),
                        item -> saved.postValue(true),
                        error -> failSaving());
            } catch (OutOfMemoryError e) {
                failSaving();
            }
        });
    }

    private void failSaving() {
        fail(R.string.scan_error_save);
        saving.set(false);
        busy.postValue(false);
    }

    /** Renders a small preview; a newer request makes older ones that are still queued or running discard their result. */
    private void requestPreview() {
        int request = latestPreviewRequest.incrementAndGet();
        Quad quad = corners.getValue();
        ScanOptions chosen = options.getValue();
        busy.setValue(true);
        executor.execute(() -> {
            if (request != latestPreviewRequest.get()) {
                return;
            }
            try {
                ScanResult result = ScanPipeline.process(previewSource, quad, chosen, PREVIEW_MAX_SIDE);
                if (request == latestPreviewRequest.get()) {
                    preview.postValue(BitmapCodec.toBitmap(result.getImage()));
                    busy.postValue(false);
                }
            } catch (OutOfMemoryError e) {
                fail(R.string.scan_error_preview);
                busy.postValue(false);
            }
        });
    }

    private void fail(@StringRes int message) {
        errorMessage.postValue(message);
    }

    private static String defaultTitle() {
        return "Scan " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
    }
}
