package com.yurii.pavlenko.myassistant.scan.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yurii.pavlenko.myassistant.databinding.FragmentScanBinding;
import com.yurii.pavlenko.myassistant.scan.export.ExportFormat;
import com.yurii.pavlenko.myassistant.scan.export.ScanExporter;
import com.yurii.pavlenko.myassistant.scan.model.ScanDocument;
import com.yurii.pavlenko.myassistant.scan.model.ScanPage;
import com.yurii.pavlenko.myassistant.scan.repository.ScanRepository;
import com.yurii.pavlenko.myassistant.scan.viewmodel.ScanViewModel;

import java.util.List;
import java.util.function.Consumer;


/** The Scan tab: start a new document from a photo, and manage the saved documents. */
public class ScanFragment extends Fragment implements ScanActionsDialog.Listener {

    private FragmentScanBinding binding;
    private ScanViewModel viewModel;
    private ScanFileActions fileActions;
    private ScanThumbnailLoader thumbnailLoader;
    private ScanPhotoSource photoSource;

    private final ActivityResultLauncher<String> savePdfLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument(ExportFormat.PDF.getMimeType()),
            this::saveToChosenLocation);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photoSource = new ScanPhotoSource(this, requireContext(), this::openEditor, () -> { });
    }

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
        fileActions = new ScanFileActions(requireContext());
        thumbnailLoader = new ScanThumbnailLoader();

        ScanListAdapter adapter = new ScanListAdapter(thumbnailLoader,
                item -> ScanActionsDialog.show(requireContext(), item.document, this));
        binding.scanRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.scanRecycler.setAdapter(adapter);

        binding.cameraButton.setOnClickListener(v -> photoSource.takePhoto());
        binding.galleryButton.setOnClickListener(v -> photoSource.pickPhoto());

        viewModel.getDocuments().observe(getViewLifecycleOwner(), documents -> {
            adapter.submitList(documents);
            binding.emptyText.setVisibility(documents.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        thumbnailLoader.release();
        binding = null;
    }

    @Override
    public void onOpenPages(ScanDocument document) {
        startActivity(ScanDocumentActivity.createIntent(requireContext(), document.getId()));
    }

    @Override
    public void onShare(ScanDocument document, ExportFormat format) {
        withPages(document, pages -> fileActions.share(document.getTitle(), pages, format));
    }

    @Override
    public void onSavePdfTo(ScanDocument document) {
        viewModel.setPendingSave(document);
        savePdfLauncher.launch(ScanExporter.suggestedFileName(document.getTitle(), ExportFormat.PDF));
    }

    @Override
    public void onRename(ScanDocument document, String title) {
        viewModel.rename(document, title);
    }

    @Override
    public void onDelete(ScanDocument document) {
        viewModel.delete(document);
    }

    private void openEditor(Uri photo) {
        startActivity(ScanEditorActivity.createIntent(requireContext(), photo, ScanRepository.NEW_DOCUMENT));
    }

    private void saveToChosenLocation(@Nullable Uri target) {
        ScanDocument document = viewModel.takePendingSave();
        if (target != null && document != null) {
            withPages(document, pages -> fileActions.saveTo(document.getTitle(), pages, ExportFormat.PDF, target));
        }
    }

    /** Pages load off the main thread; the continuation runs back on it, only if the screen is still there. */
    private void withPages(ScanDocument document, Consumer<List<ScanPage>> action) {
        viewModel.loadPages(document, pages -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> action.accept(pages));
            }
        });
    }
}
