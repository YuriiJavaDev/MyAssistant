package com.yurii.pavlenko.myassistant.scan.ui;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.lifecycle.LifecycleOwner;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.ActivityScanEditorBinding;
import com.yurii.pavlenko.myassistant.scan.imaging.PaperFormat;
import com.yurii.pavlenko.myassistant.scan.imaging.ScanFilter;
import com.yurii.pavlenko.myassistant.scan.imaging.ScanOptions;
import com.yurii.pavlenko.myassistant.scan.viewmodel.ScanEditorViewModel;

/** Connects the second editing step's controls (filter, paper, switches, rotate, save) with the view model. */
final class ScanAdjustPanel {

    private final ActivityScanEditorBinding binding;
    private final ScanEditorViewModel viewModel;
    private final PaperFormat[] formats = PaperFormat.values();
    private boolean reflectingState;

    ScanAdjustPanel(ActivityScanEditorBinding binding, ScanEditorViewModel viewModel) {
        this.binding = binding;
        this.viewModel = viewModel;
    }

    void bind(LifecycleOwner owner) {
        setupFilterChips();
        setupPaperSpinner();
        setupSwitches();
        binding.rotateButton.setOnClickListener(v -> viewModel.updateOptions(ScanOptions::rotatedClockwise));
        binding.backButton.setOnClickListener(v -> viewModel.backToCrop());
        binding.saveButton.setOnClickListener(v -> viewModel.save());
        viewModel.getOptions().observe(owner, this::reflect);
    }

    private void setupFilterChips() {
        binding.filterChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!reflectingState && !checkedIds.isEmpty()) {
                viewModel.updateOptions(options -> options.withFilter(filterOfChip(checkedIds.get(0))));
            }
        });
    }

    private void setupPaperSpinner() {
        String[] labels = new String[formats.length];
        for (int i = 0; i < formats.length; i++) {
            labels[i] = formats[i].getLabel();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(binding.getRoot().getContext(), R.layout.item_spinner, labels);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        binding.paperSpinner.setAdapter(adapter);
        binding.paperSpinner.setPopupBackgroundResource(R.drawable.bg_spinner_dropdown);
        binding.paperSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean changed = formats[position] != viewModel.getOptions().getValue().getPaperFormat();
                if (!reflectingState && changed) {
                    viewModel.updateOptions(options -> options.withPaperFormat(formats[position]));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSwitches() {
        binding.shadowsSwitch.setOnCheckedChangeListener((button, checked) -> {
            if (!reflectingState) {
                viewModel.updateOptions(options -> options.withRemoveShadows(checked));
            }
        });
        binding.sharpenSwitch.setOnCheckedChangeListener((button, checked) -> {
            if (!reflectingState) {
                viewModel.updateOptions(options -> options.withSharpen(checked));
            }
        });
    }

    /** Shows the current options in the widgets without triggering their change listeners. */
    private void reflect(ScanOptions options) {
        reflectingState = true;
        binding.filterChips.check(chipOfFilter(options.getFilter()));
        binding.paperSpinner.setSelection(options.getPaperFormat().ordinal());
        binding.shadowsSwitch.setChecked(options.isRemoveShadows());
        binding.sharpenSwitch.setChecked(options.isSharpen());
        reflectingState = false;
    }

    private static ScanFilter filterOfChip(int chipId) {
        if (chipId == R.id.chipGray) {
            return ScanFilter.GRAYSCALE;
        }
        return chipId == R.id.chipBlackWhite ? ScanFilter.BLACK_WHITE : ScanFilter.COLOR;
    }

    private static int chipOfFilter(ScanFilter filter) {
        switch (filter) {
            case GRAYSCALE:
                return R.id.chipGray;
            case BLACK_WHITE:
                return R.id.chipBlackWhite;
            default:
                return R.id.chipColor;
        }
    }
}
