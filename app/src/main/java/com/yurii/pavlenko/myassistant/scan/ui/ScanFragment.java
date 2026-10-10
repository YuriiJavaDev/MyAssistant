package com.yurii.pavlenko.myassistant.scan.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.FragmentScanBinding;
import com.yurii.pavlenko.myassistant.scan.export.ExportFormat;
import com.yurii.pavlenko.myassistant.scan.export.ScanExporter;
import com.yurii.pavlenko.myassistant.scan.export.ShareIntents;
import com.yurii.pavlenko.myassistant.scan.files.ScanCache;
import com.yurii.pavlenko.myassistant.scan.model.ScanItem;
import com.yurii.pavlenko.myassistant.scan.viewmodel.ScanViewModel;

import java.io.File;
import java.io.IOException;

/** The Scan tab: shoot or pick a photo, open the editor, and manage saved scans. */
public class ScanFragment extends Fragment implements ScanActionsDialog.Listener {

    private FragmentScanBinding binding;
    private ScanViewModel viewModel;
    private ScanExporter exporter;
    private ScanThumbnailLoader thumbnailLoader;

    private final ActivityResultLauncher<Uri> takePhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success) {
                    openEditor(ScanCache.uriFor(requireContext(), ScanCache.captureFile(requireContext())));
                }
            });

    private final ActivityResultLauncher<PickVisualMediaRequest> pickPhotoLauncher = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
                if (uri != null) {
                    openEditor(uri);
                }
            });

    private final ActivityResultLauncher<String> savePdfLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument(ExportFormat.PDF.getMimeType()),
            uri -> saveToChosenLocation(uri, ExportFormat.PDF));

    private final ActivityResultLauncher<String> saveJpegLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument(ExportFormat.JPEG.getMimeType()),
            uri -> saveToChosenLocation(uri, ExportFormat.JPEG));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentScanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ScanViewModel.class);
        exporter = new ScanExporter(requireContext());
        thumbnailLoader = new ScanThumbnailLoader();

        ScanListAdapter adapter = new ScanListAdapter(thumbnailLoader,
                item -> ScanActionsDialog.show(requireContext(), item, this));
        binding.scanRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.scanRecycler.setAdapter(adapter);

        binding.cameraButton.setOnClickListener(v -> takePhoto());
        binding.galleryButton.setOnClickListener(v -> pickPhotoLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        viewModel.getScans().observe(getViewLifecycleOwner(), scans -> {
            adapter.submitList(scans);
            binding.emptyText.setVisibility(scans.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thumbnailLoader.release();
        binding = null;
    }

    @Override
    public void onOpen(ScanItem item) {
        exporter.export(item, ExportFormat.JPEG, new ScanExporter.Callback() {
            @Override
            public void onReady(File file) {
                startActivitySafely(ShareIntents.view(requireContext(), file, ExportFormat.JPEG), R.string.scan_error_no_viewer);
            }

            @Override
            public void onFailed(IOException error) {
                showMessage(R.string.scan_error_export);
            }
        });
    }

    @Override
    public void onShare(ScanItem item, ExportFormat format) {
        exporter.export(item, format, new ScanExporter.Callback() {
            @Override
            public void onReady(File file) {
                startActivitySafely(ShareIntents.send(requireContext(), file, format, getString(R.string.scan_share_title)), R.string.scan_error_no_app);
            }

            @Override
            public void onFailed(IOException error) {
                showMessage(R.string.scan_error_export);
            }
        });
    }

    @Override
    public void onSaveTo(ScanItem item, ExportFormat format) {
        viewModel.setPendingSave(item);
        String suggestedName = ScanExporter.suggestedFileName(item, format);
        (format == ExportFormat.PDF ? savePdfLauncher : saveJpegLauncher).launch(suggestedName);
    }

    @Override
    public void onRename(ScanItem item, String title) {
        viewModel.rename(item, title);
    }

    @Override
    public void onDelete(ScanItem item) {
        viewModel.delete(item);
    }

    private void takePhoto() {
        File captureFile = ScanCache.captureFile(requireContext());
        captureFile.delete();
        try {
            takePhotoLauncher.launch(ScanCache.uriFor(requireContext(), captureFile));
        } catch (ActivityNotFoundException e) {
            showMessage(R.string.scan_error_no_camera);
        }
    }

    private void openEditor(Uri photo) {
        startActivity(ScanEditorActivity.createIntent(requireContext(), photo));
    }

    private void saveToChosenLocation(@Nullable Uri target, ExportFormat format) {
        ScanItem item = viewModel.takePendingSave();
        if (target == null || item == null) {
            return;
        }
        exporter.exportToUri(item, format, target, new ScanExporter.Callback() {
            @Override
            public void onReady(File file) {
                showMessage(R.string.scan_saved_to_device);
            }

            @Override
            public void onFailed(IOException error) {
                showMessage(R.string.scan_error_export);
            }
        });
    }

    private void startActivitySafely(Intent intent, int missingAppMessage) {
        if (!isAdded()) {
            return;
        }
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showMessage(missingAppMessage);
        }
    }

    private void showMessage(int message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
