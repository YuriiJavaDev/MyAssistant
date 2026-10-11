package com.yurii.pavlenko.myassistant.scan.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yurii.pavlenko.myassistant.databinding.ActivityScanDocumentBinding;
import com.yurii.pavlenko.myassistant.scan.export.ExportFormat;
import com.yurii.pavlenko.myassistant.scan.export.ScanExporter;
import com.yurii.pavlenko.myassistant.scan.model.ScanPage;
import com.yurii.pavlenko.myassistant.scan.viewmodel.ScanDocumentViewModel;

import java.util.Collections;
import java.util.List;

/** The pages of one document: add pages, reorder, view, export or delete them, and share the whole PDF. */
public class ScanDocumentActivity extends AppCompatActivity implements ScanPageActionsDialog.Listener {

    private static final String EXTRA_DOCUMENT_ID = "extra_document_id";

    public static Intent createIntent(Context context, long documentId) {
        return new Intent(context, ScanDocumentActivity.class).putExtra(EXTRA_DOCUMENT_ID, documentId);
    }

    private final ActivityResultLauncher<String> saveJpegLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument(ExportFormat.JPEG.getMimeType()),
            this::saveToChosenLocation);

    private ActivityScanDocumentBinding binding;
    private ScanDocumentViewModel viewModel;
    private ScanThumbnailLoader thumbnailLoader;
    private ScanFileActions fileActions;
    private ScanPhotoSource photoSource;
    private long documentId;
    private String title = "";
    private List<ScanPage> pages = Collections.emptyList();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanDocumentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        documentId = getIntent().getLongExtra(EXTRA_DOCUMENT_ID, -1);
        viewModel = new ViewModelProvider(this,
                new ScanDocumentViewModel.Factory(getApplication(), documentId)).get(ScanDocumentViewModel.class);
        thumbnailLoader = new ScanThumbnailLoader();
        fileActions = new ScanFileActions(this);
        photoSource = new ScanPhotoSource(this, this, this::openEditor, () -> { });

        ScanPageAdapter adapter = new ScanPageAdapter(thumbnailLoader,
                page -> ScanPageActionsDialog.show(this, title, page, this));
        binding.pageRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.pageRecycler.setAdapter(adapter);

        binding.cameraButton.setOnClickListener(v -> photoSource.takePhoto());
        binding.galleryButton.setOnClickListener(v -> photoSource.pickPhoto());
        binding.shareButton.setOnClickListener(v -> fileActions.share(title, pages, ExportFormat.PDF));

        viewModel.getDocument().observe(this, document -> {
            if (document != null) {
                title = document.getTitle();
                binding.documentTitle.setText(title);
            }
        });
        viewModel.getPages().observe(this, newPages -> {
            pages = newPages;
            adapter.submitList(newPages);
            if (newPages.isEmpty()) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        thumbnailLoader.release();
    }

    @Override
    public void onView(ScanPage page) {
        fileActions.view(title, page, ExportFormat.JPEG);
    }

    @Override
    public void onSaveJpegTo(ScanPage page) {
        viewModel.setPendingSavePage(page);
        saveJpegLauncher.launch(ScanExporter.suggestedFileName(title, ExportFormat.JPEG));
    }

    @Override
    public void onMove(ScanPage page, int direction) {
        viewModel.movePage(pages, pages.indexOf(page), direction);
    }

    @Override
    public void onDelete(ScanPage page) {
        viewModel.deletePage(page);
    }

    private void openEditor(Uri photo) {
        startActivity(ScanEditorActivity.createIntent(this, photo, documentId));
    }

    private void saveToChosenLocation(@Nullable Uri target) {
        ScanPage page = viewModel.takePendingSavePage();
        if (target != null && page != null) {
            fileActions.saveTo(title, Collections.singletonList(page), ExportFormat.JPEG, target);
        }
    }
}
