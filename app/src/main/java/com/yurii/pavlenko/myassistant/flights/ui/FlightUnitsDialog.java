package com.yurii.pavlenko.myassistant.flights.ui;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.yurii.pavlenko.myassistant.databinding.DialogFlightUnitsBinding;
import com.yurii.pavlenko.myassistant.flights.data.FlightUnits;
import com.yurii.pavlenko.myassistant.flights.logic.AltitudeUnit;
import com.yurii.pavlenko.myassistant.flights.logic.SpeedUnit;
import com.yurii.pavlenko.myassistant.flights.viewmodel.FlightsViewModel;

/** Choice of the speed and altitude units; each choice applies at once. */
public class FlightUnitsDialog extends DialogFragment {

    private FlightsViewModel viewModel;
    private DialogFlightUnitsBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogFlightUnitsBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(requireParentFragment()).get(FlightsViewModel.class);

        FlightUnits current = viewModel.getUnits().getValue();
        binding.speedGroup.check(speedButtonId(current.getSpeed()));
        binding.altitudeGroup.check(current.getAltitude() == AltitudeUnit.FEET
                ? binding.feet.getId() : binding.metres.getId());
        binding.speedGroup.setOnCheckedChangeListener((group, id) -> save());
        binding.altitudeGroup.setOnCheckedChangeListener((group, id) -> save());

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(binding.getRoot()).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    private int speedButtonId(SpeedUnit unit) {
        switch (unit) {
            case KNOTS: return binding.knots.getId();
            case METRES_PER_SECOND: return binding.metresPerSecond.getId();
            default: return binding.kilometresPerHour.getId();
        }
    }

    private void save() {
        int speedId = binding.speedGroup.getCheckedRadioButtonId();
        SpeedUnit speed = speedId == binding.knots.getId() ? SpeedUnit.KNOTS
                : speedId == binding.metresPerSecond.getId() ? SpeedUnit.METRES_PER_SECOND
                : SpeedUnit.KILOMETRES_PER_HOUR;
        AltitudeUnit altitude = binding.altitudeGroup.getCheckedRadioButtonId() == binding.feet.getId()
                ? AltitudeUnit.FEET : AltitudeUnit.METRES;
        viewModel.saveUnits(new FlightUnits(speed, altitude));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
