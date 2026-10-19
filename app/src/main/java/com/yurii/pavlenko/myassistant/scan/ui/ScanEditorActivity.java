package com.yurii.pavlenko.myassistant.scan.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.ActivityScanEditorBinding;
import com.yurii.pavlenko.myassistant.scan.repository.ScanRepository;
import com.yurii.pavlenko.myassistant.scan.viewmodel.ScanEditorViewModel;
import com.yurii.pavlenko.myassistant.scan.viewmodel.ScanEditorViewModel.Stage;
import com.yurii.pavlenko.myassistant.ui.theme.DialogStyle;

/**
 * Two steps on one photo: place the page corners, then choose how the scan should look and save it.
 * After saving, the next page of the same document can be scanned without leaving the editor.
 */
public class ScanEditorActivity extends AppCompatActivity {

    private static final String EXTRA_DOCUMENT_ID = "extra_document_id";

    /** Pass ScanRepository.NEW_DOCUMENT to start a new document, or an existing id to add a page to it. */
    public static Intent createIntent(Context context, Uri photo, long documentId) {
        return new Intent(context, ScanEditorActivity.class)
                .setData(photo)
                .putExtra(EXTRA_DOCUMENT_ID, documentId)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    private ActivityScanEditorBinding binding;
    private ScanEditorViewModel viewModel;
    private ScanPhotoSource photoSource;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Uri photo = getIntent().getData();
        if (photo == null) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ScanEditorViewModel.class);
        viewModel.setDocumentId(getIntent().getLongExtra(EXTRA_DOCUMENT_ID, ScanRepository.NEW_DOCUMENT));
        viewModel.load(photo);
        photoSource = new ScanPhotoSource(this, this, viewModel::restart, this::finish);

        new ScanAdjustPanel(binding, viewModel).bind(this);
        setupCropStep();
        observeViewModel();
    }

    private void setupCropStep() {
        binding.cropView.setOnQuadChangedListener(viewModel::setCorners);
        binding.autoButton.setOnClickListener(v -> viewModel.detectCorners());
        binding.resetButton.setOnClickListener(v -> viewModel.resetCorners());
        binding.nextButton.setOnClickListener(v -> viewModel.goToAdjust());
    }

    private void observeViewModel() {
        viewModel.getSourceBitmap().observe(this, binding.cropView::setBitmap);
        viewModel.getCorners().observe(this, binding.cropView::setQuad);
        viewModel.getPreview().observe(this, binding.previewImage::setImageBitmap);
        viewModel.getStage().observe(this, this::showStage);
        viewModel.isBusy().observe(this, busy -> binding.progress.setVisibility(busy ? View.VISIBLE : View.GONE));
        viewModel.getErrorMessage().observe(this, this::showError);
        viewModel.getSavedDocumentId().observe(this, savedTo -> {
            if (savedTo != null) {
                askForNextPage();
            }
        });
    }

    private void askForNextPage() {
        DialogStyle.show(new AlertDialog.Builder(this)
                .setTitle(R.string.scan_page_saved)
                .setMessage(R.string.scan_next_page_question)
                .setCancelable(false)
                .setPositiveButton(R.string.scan_camera, (dialog, which) -> {
                    viewModel.consumeSaved();
                    photoSource.takePhoto();
                })
                .setNeutralButton(R.string.scan_gallery, (dialog, which) -> {
                    viewModel.consumeSaved();
                    photoSource.pickPhoto();
                })
                .setNegativeButton(R.string.scan_done, (dialog, which) -> finish()));
    }

    private void showStage(Stage stage) {
        boolean cropping = stage == Stage.CROP;
        boolean adjusting = stage == Stage.ADJUST;
        binding.cropView.setVisibility(cropping ? View.VISIBLE : View.INVISIBLE);
        binding.cropControls.setVisibility(cropping ? View.VISIBLE : View.GONE);
        binding.previewImage.setVisibility(adjusting ? View.VISIBLE : View.GONE);
        binding.adjustControls.setVisibility(adjusting ? View.VISIBLE : View.GONE);
    }

    private void showError(@Nullable Integer message) {
        if (message == null) {
            return;
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        viewModel.clearError();
        if (viewModel.getStage().getValue() == Stage.LOADING) {
            finish();
        }
    }
}
