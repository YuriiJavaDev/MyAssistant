package com.yurii.pavlenko.myassistant.calculator.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.calculator.logic.AngleMode;
import com.yurii.pavlenko.myassistant.calculator.logic.CalculatorDisplay;
import com.yurii.pavlenko.myassistant.calculator.logic.HistoryEntry;
import com.yurii.pavlenko.myassistant.calculator.viewmodel.CalculatorViewModel;
import com.yurii.pavlenko.myassistant.databinding.FragmentCalculatorBinding;

import java.util.List;

/**
 * The Calculator tab. Portrait shows a basic calculator with a panel of scientific keys that can be
 * opened; landscape shows the full engineering calculator with a numeric keypad on the right.
 */
public class CalculatorFragment extends Fragment {

    private FragmentCalculatorBinding binding;
    private CalculatorViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCalculatorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CalculatorViewModel.class);

        KeypadBinder.bind(binding.getRoot(), viewModel::press);
        binding.display.angleButton.setOnClickListener(v -> viewModel.toggleAngleMode());
        binding.display.lastCalculation.setOnClickListener(v -> showHistory());
        if (binding.scientificToggle != null) {
            binding.scientificToggle.setOnClickListener(v -> {
                viewModel.toggleScientific();
                showScientificPanel();
            });
            showScientificPanel();
        }
        viewModel.getDisplay().observe(getViewLifecycleOwner(), this::render);
    }

    private void showScientificPanel() {
        boolean shown = viewModel.isScientificShown();
        binding.scientificPanel.setVisibility(shown ? View.VISIBLE : View.GONE);
        binding.scientificToggle.setText(shown ? R.string.calculator_basic : R.string.calculator_scientific);
    }

    private void render(CalculatorDisplay display) {
        binding.display.expressionText.setText(display.getExpression());
        binding.display.previewText.setText(display.getError() != null ? display.getError()
                : display.getPreview() == null ? "" : getString(R.string.calculator_preview_format, display.getPreview()));
        binding.display.memoryText.setText(display.getMemory() == null ? ""
                : getString(R.string.calculator_memory_format, display.getMemory()));
        binding.display.angleButton.setText(display.getAngleMode() == AngleMode.DEGREES
                ? R.string.calculator_degrees : R.string.calculator_radians);
        HistoryEntry last = display.getLast();
        binding.display.lastCalculation.setText(last == null ? "" : getString(R.string.calculator_last_format,
                last.getExpression(), last.getResult()));
        binding.display.lastCalculation.setEnabled(last != null);
    }

    private void showHistory() {
        List<HistoryEntry> entries = viewModel.history();
        if (entries.isEmpty()) {
            return;
        }
        CharSequence[] lines = new CharSequence[entries.size()];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = entries.get(i).toString();
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.calculator_history)
                .setItems(lines, (dialog, which) -> viewModel.restore(entries.get(which)))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // memory leak prevention
    }
}
