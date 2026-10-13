package com.yurii.pavlenko.myassistant.steps.ui;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.DialogStepSettingsBinding;
import com.yurii.pavlenko.myassistant.steps.logic.StepLengths;
import com.yurii.pavlenko.myassistant.steps.viewmodel.StepsViewModel;

import java.math.BigDecimal;

/** Step lengths for walking and running, with a reminder of how to measure them. */
public class StepSettingsDialog extends DialogFragment {

    private DialogStepSettingsBinding binding;
    private StepsViewModel viewModel;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogStepSettingsBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(requireParentFragment()).get(StepsViewModel.class);

        StepLengths current = viewModel.getLengths().getValue();
        show(current == null ? StepLengths.defaults() : current);

        binding.defaultsButton.setOnClickListener(v -> show(StepLengths.defaults()));
        binding.saveButton.setOnClickListener(v -> save());

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(binding.getRoot()).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    private void show(StepLengths lengths) {
        binding.walkInput.setText(plain(lengths.getWalkCm()));
        binding.runInput.setText(plain(lengths.getRunCm()));
        binding.cadenceInput.setText(String.valueOf(lengths.getRunCadence()));
    }

    private void save() {
        Double walk = readLength(binding.walkInput, binding.walkLayout);
        Double run = readLength(binding.runInput, binding.runLayout);
        Integer cadence = readCadence();
        if (walk == null || run == null || cadence == null) {
            return;
        }
        viewModel.saveLengths(new StepLengths(walk, run, cadence));
        dismiss();
    }

    private Double readLength(TextInputEditText input, TextInputLayout layout) {
        Double value = parse(input);
        boolean valid = value != null && StepLengths.isValidLength(value);
        layout.setError(valid ? null : getString(R.string.steps_invalid_length,
                (int) StepLengths.MIN_LENGTH_CM, (int) StepLengths.MAX_LENGTH_CM));
        return valid ? value : null;
    }

    private Integer readCadence() {
        Double value = parse(binding.cadenceInput);
        boolean valid = value != null && StepLengths.isValidCadence((int) Math.round(value));
        binding.cadenceLayout.setError(valid ? null : getString(R.string.steps_invalid_length,
                StepLengths.MIN_RUN_CADENCE, StepLengths.MAX_RUN_CADENCE));
        return valid ? (int) Math.round(value) : null;
    }

    private static Double parse(TextInputEditText input) {
        try {
            return Double.parseDouble(String.valueOf(input.getText()).trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String plain(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
