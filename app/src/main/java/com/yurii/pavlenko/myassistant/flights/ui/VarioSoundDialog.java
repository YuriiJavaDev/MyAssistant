package com.yurii.pavlenko.myassistant.flights.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.DialogVarioSoundBinding;
import com.yurii.pavlenko.myassistant.flights.logic.VarioSoundSettings;
import com.yurii.pavlenko.myassistant.flights.viewmodel.FlightsViewModel;

/** Lets the pilot set the climb and sink rates at which the variometer changes its sound. */
public class VarioSoundDialog extends DialogFragment {

    private static final double STEP = 0.1;

    private FlightsViewModel viewModel;
    private DialogVarioSoundBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogVarioSoundBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(requireParentFragment()).get(FlightsViewModel.class);

        VarioSoundSettings current = viewModel.getSound().getValue();
        setUp(binding.climbStartSeek, binding.climbStartLabel, R.string.flights_sound_climb_start,
                VarioSoundSettings.CLIMB_START_MIN, VarioSoundSettings.CLIMB_START_MAX, current.getClimbStart());
        setUp(binding.sinkStartSeek, binding.sinkStartLabel, R.string.flights_sound_sink_start,
                VarioSoundSettings.SINK_START_MIN, VarioSoundSettings.SINK_START_MAX, current.getSinkStart());
        setUp(binding.sinkAlarmSeek, binding.sinkAlarmLabel, R.string.flights_sound_sink_alarm,
                VarioSoundSettings.SINK_ALARM_MIN, VarioSoundSettings.SINK_ALARM_MAX, current.getSinkAlarm());

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(binding.getRoot()).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    private void setUp(SeekBar seek, TextView label, int labelRes, double min, double max, double value) {
        seek.setMax((int) Math.round((max - min) / STEP));
        seek.setProgress((int) Math.round((value - min) / STEP));
        label.setText(getString(labelRes, value));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                label.setText(getString(labelRes, min + progress * STEP));
                if (fromUser) {
                    save();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
            }
        });
    }

    private void save() {
        VarioSoundSettings current = viewModel.getSound().getValue();
        viewModel.saveSound(new VarioSoundSettings(current.isEnabled(),
                VarioSoundSettings.CLIMB_START_MIN + binding.climbStartSeek.getProgress() * STEP,
                VarioSoundSettings.SINK_START_MIN + binding.sinkStartSeek.getProgress() * STEP,
                VarioSoundSettings.SINK_ALARM_MIN + binding.sinkAlarmSeek.getProgress() * STEP));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
