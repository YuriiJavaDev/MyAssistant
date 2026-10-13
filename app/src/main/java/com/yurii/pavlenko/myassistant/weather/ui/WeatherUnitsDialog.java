package com.yurii.pavlenko.myassistant.weather.ui;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.yurii.pavlenko.myassistant.databinding.DialogWeatherUnitsBinding;
import com.yurii.pavlenko.myassistant.weather.data.WeatherUnits;
import com.yurii.pavlenko.myassistant.weather.logic.TemperatureUnit;
import com.yurii.pavlenko.myassistant.weather.logic.WindUnit;
import com.yurii.pavlenko.myassistant.weather.viewmodel.WeatherViewModel;

/** Choice of the temperature and wind speed units; each choice applies at once. */
public class WeatherUnitsDialog extends DialogFragment {

    private WeatherViewModel viewModel;
    private DialogWeatherUnitsBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DialogWeatherUnitsBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(requireParentFragment()).get(WeatherViewModel.class);

        WeatherUnits current = viewModel.getUnits().getValue();
        binding.temperatureGroup.check(current.getTemperature() == TemperatureUnit.FAHRENHEIT
                ? binding.fahrenheit.getId() : binding.celsius.getId());
        binding.windGroup.check(current.getWind() == WindUnit.KILOMETRES_PER_HOUR
                ? binding.kilometresPerHour.getId() : binding.metresPerSecond.getId());
        binding.temperatureGroup.setOnCheckedChangeListener((group, id) -> save());
        binding.windGroup.setOnCheckedChangeListener((group, id) -> save());

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(binding.getRoot()).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    private void save() {
        TemperatureUnit temperature = binding.temperatureGroup.getCheckedRadioButtonId() == binding.fahrenheit.getId()
                ? TemperatureUnit.FAHRENHEIT : TemperatureUnit.CELSIUS;
        WindUnit wind = binding.windGroup.getCheckedRadioButtonId() == binding.kilometresPerHour.getId()
                ? WindUnit.KILOMETRES_PER_HOUR : WindUnit.METRES_PER_SECOND;
        viewModel.saveUnits(new WeatherUnits(temperature, wind));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
