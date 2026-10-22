package com.yurii.pavlenko.myassistant.weather.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.FragmentWeatherBinding;
import com.yurii.pavlenko.myassistant.weather.data.CurrentWeather;
import com.yurii.pavlenko.myassistant.weather.data.Place;
import com.yurii.pavlenko.myassistant.weather.data.WeatherUnits;
import com.yurii.pavlenko.myassistant.weather.logic.BeachScenario;
import com.yurii.pavlenko.myassistant.weather.logic.MoonPhase;
import com.yurii.pavlenko.myassistant.weather.logic.WeatherCondition;
import com.yurii.pavlenko.myassistant.weather.logic.WeatherCode;
import com.yurii.pavlenko.myassistant.weather.ui.scene.SceneFactory;
import com.yurii.pavlenko.myassistant.weather.viewmodel.WeatherState;
import com.yurii.pavlenko.myassistant.weather.viewmodel.WeatherViewModel;
import com.yurii.pavlenko.myassistant.ui.theme.DialogStyle;

import java.util.List;

/** The Weather tab: current weather where the phone is, or in any city the user searches for. */
public class WeatherFragment extends Fragment {

    private static final String UNITS_TAG = "weather_units";

    private FragmentWeatherBinding binding;
    private WeatherViewModel viewModel;
    private boolean permissionBlocked;
    private WeatherState shown;

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                permissionBlocked = !granted
                        && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION);
                if (granted) {
                    viewModel.showPhoneLocation();
                } else {
                    render(viewModel.getState().getValue());
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWeatherBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        binding.searchButton.setOnClickListener(v -> search());
        binding.cityInput.setOnEditorActionListener((input, action, event) -> {
            if (action == EditorInfo.IME_ACTION_SEARCH) {
                search();
                return true;
            }
            return false;
        });
        binding.myLocationButton.setOnClickListener(v -> viewModel.showPhoneLocation());
        binding.accessButton.setOnClickListener(v -> onAccessButton());
        binding.unitsButton.setOnClickListener(v -> new WeatherUnitsDialog().show(getChildFragmentManager(), UNITS_TAG));

        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        viewModel.getUnits().observe(getViewLifecycleOwner(), units -> render(shown));
        viewModel.getMatches().observe(getViewLifecycleOwner(), this::offerMatches);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refreshIfStale();
    }

    private void search() {
        hideKeyboard();
        viewModel.search(String.valueOf(binding.cityInput.getText()));
    }

    private void hideKeyboard() {
        InputMethodManager manager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(binding.cityInput.getWindowToken(), 0);
        }
    }

    private void onAccessButton() {
        if (permissionBlocked) {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireContext().getPackageName(), null)));
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }

    private void offerMatches(@Nullable List<Place> places) {
        if (places == null) {
            return;
        }
        String[] names = new String[places.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = places.get(i).fullName();
        }
        DialogStyle.show(new AlertDialog.Builder(requireContext())
                .setTitle(R.string.weather_choose_city)
                .setItems(names, (dialog, which) -> viewModel.showPlace(places.get(which))));
        viewModel.clearMatches();
    }

    private void render(@Nullable WeatherState state) {
        if (state == null || binding == null) {
            return;
        }
        shown = state;
        WeatherState.Status status = state.getStatus();
        binding.content.setVisibility(status == WeatherState.Status.CONTENT ? View.VISIBLE : View.GONE);
        binding.accessPanel.setVisibility(status == WeatherState.Status.NEEDS_LOCATION_PERMISSION ? View.VISIBLE : View.GONE);
        binding.accessButton.setText(permissionBlocked ? R.string.weather_open_settings : R.string.weather_allow);

        boolean showsStatus = status == WeatherState.Status.LOADING || status == WeatherState.Status.ERROR;
        binding.statusText.setVisibility(showsStatus ? View.VISIBLE : View.GONE);
        if (status == WeatherState.Status.LOADING) {
            binding.statusText.setText(R.string.weather_loading);
        } else if (status == WeatherState.Status.ERROR) {
            binding.statusText.setText(getString(WeatherTexts.error(state.getError()), state.getErrorArgument()));
        } else if (status == WeatherState.Status.CONTENT) {
            renderWeather(state.getPlace(), state.getWeather());
        }
    }

    private void renderWeather(Place place, CurrentWeather weather) {
        binding.cityName.setText(place.hasName() ? place.getName() : getString(R.string.weather_current_location));
        binding.countryName.setText(place.getCountry().isEmpty() ? "" : getString(R.string.weather_country_format, place.getCountry()));
        binding.countryName.setVisibility(place.getCountry().isEmpty() ? View.GONE : View.VISIBLE);

        WeatherUnits units = viewModel.getUnits().getValue();
        WeatherCondition condition = WeatherCode.condition(weather.getWeatherCode());
        BeachScenario scenario = BeachScenario.of(condition, weather.isDay(), weather.getTemperature(), weather.getWindSpeed());
        binding.porthole.show(SceneFactory.weather(condition, weather.isDay(), scenario));
        binding.temperature.setText(units.getTemperature().format(weather.getTemperature()));
        binding.description.setText(getString(WeatherTexts.description(WeatherCode.describe(weather.getWeatherCode()))));

        binding.detailFeelsLike.setText(units.getTemperature().format(weather.getFeelsLike()));
        binding.detailHumidity.setText(getString(R.string.weather_humidity_value, weather.getHumidity()));
        binding.detailWind.setText(units.getWind().format(weather.getWindSpeed()));
        binding.detailRange.setText(units.getTemperature().formatShort(weather.getTodayMax())
                + " / " + units.getTemperature().formatShort(weather.getTodayMin()));
        renderMoon(place);
    }

    private void renderMoon(Place place) {
        MoonPhase phase = MoonPhase.at(System.currentTimeMillis());
        binding.moonWindow.show(SceneFactory.moon(phase, place.getLatitude() < 0));
        binding.moonCaption.setText(getString(R.string.weather_moon_caption,
                getString(WeatherTexts.moonPhase(phase.phase())), Math.round(phase.illumination() * 100)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
