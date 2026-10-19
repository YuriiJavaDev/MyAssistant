package com.yurii.pavlenko.myassistant.flights.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.yurii.pavlenko.myassistant.databinding.DialogFlightUnitsBinding;
import com.yurii.pavlenko.myassistant.flights.data.FlightUnits;
import com.yurii.pavlenko.myassistant.flights.logic.AltitudeUnit;
import com.yurii.pavlenko.myassistant.flights.logic.ClimbUnit;
import com.yurii.pavlenko.myassistant.flights.logic.SpeedUnit;
import com.yurii.pavlenko.myassistant.flights.viewmodel.FlightsViewModel;

/** Choice of the speed, altitude and climb rate units and of the report limit; each choice applies at once. */
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
        binding.climbGroup.check(current.getClimb() == ClimbUnit.FEET_PER_MINUTE
                ? binding.feetPerMinute.getId() : binding.metresPerSecondClimb.getId());
        binding.speedGroup.setOnCheckedChangeListener((group, id) -> save());
        binding.altitudeGroup.setOnCheckedChangeListener((group, id) -> save());
        binding.climbGroup.setOnCheckedChangeListener((group, id) -> save());
        binding.reportLimit.setText(String.valueOf(viewModel.getReportLimit()));
        binding.reportLimit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable text) {
                saveReportLimit(text.toString());
            }
        });

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
        ClimbUnit climb = binding.climbGroup.getCheckedRadioButtonId() == binding.feetPerMinute.getId()
                ? ClimbUnit.FEET_PER_MINUTE : ClimbUnit.METRES_PER_SECOND;
        viewModel.saveUnits(new FlightUnits(speed, altitude, climb));
    }

    /** A value that is not a number is ignored, so the limit is never changed by a half-typed entry. */
    private void saveReportLimit(String typed) {
        try {
            viewModel.saveReportLimit(Integer.parseInt(typed));
        } catch (NumberFormatException notANumber) {
            // The field is empty or too long to be a limit
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
