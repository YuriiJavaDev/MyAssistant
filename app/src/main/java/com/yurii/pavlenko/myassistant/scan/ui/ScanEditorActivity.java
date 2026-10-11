package com.yurii.pavlenko.myassistant.scan.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.ActivityScanEditorBinding;
import com.yurii.pavlenko.myassistant.scan.viewmodel.ScanEditorViewModel;
import com.yurii.pavlenko.myassistant.scan.viewmodel.ScanEditorViewModel.Stage;

/** Two steps on one photo: place the page corners, then choose how the scan should look and save it. */
public class ScanEditorActivity extends AppCompatActivity {

    public static Intent createIntent(Context context, Uri photo) {
        return new Intent(context, ScanEditorActivity.class)
                .setData(photo)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    private ActivityScanEditorBinding binding;
    private ScanEditorViewModel viewModel;

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
        viewModel.load(photo);

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
        viewModel.isSaved().observe(this, saved -> {
            if (saved) {
                Toast.makeText(this, R.string.scan_saved, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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
