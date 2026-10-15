package com.yurii.pavlenko.myassistant.flights.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.FragmentFlightsBinding;
import com.yurii.pavlenko.myassistant.flights.data.FlightUnits;
import com.yurii.pavlenko.myassistant.flights.logic.FlightFormat;
import com.yurii.pavlenko.myassistant.flights.logic.FlightSnapshot;
import com.yurii.pavlenko.myassistant.flights.logic.FlightStats;
import com.yurii.pavlenko.myassistant.flights.logic.FlightTime;
import com.yurii.pavlenko.myassistant.flights.logic.Orientation;
import com.yurii.pavlenko.myassistant.flights.logic.VarioSoundSettings;
import com.yurii.pavlenko.myassistant.flights.sensor.GpsTracker;
import com.yurii.pavlenko.myassistant.flights.service.FlightRecordingService;
import com.yurii.pavlenko.myassistant.flights.viewmodel.FlightsViewModel;

import java.util.Locale;
import java.util.Map;

/** The Flights tab: flight instruments and the recording of a flight. */
public class FlightsFragment extends Fragment {

    private static final String UNITS_TAG = "flight_units";
    private static final String SOUND_TAG = "vario_sound";
    private static final String JOURNAL_TAG = "flight_journal";
    private static final String RECORD_DOT = "\u25CF";

    private FragmentFlightsBinding binding;
    private FlightsViewModel viewModel;
    private boolean permissionBlocked;
    private boolean waitingToastShown;

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionResult);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFlightsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FlightsViewModel.class);

        binding.controls.recordButton.setOnClickListener(v -> onRecordButton());
        binding.controls.unitsButton.setOnClickListener(v -> new FlightUnitsDialog().show(getChildFragmentManager(), UNITS_TAG));
        binding.controls.journalButton.setOnClickListener(v -> new FlightJournalDialog().show(getChildFragmentManager(), JOURNAL_TAG));
        binding.controls.accessButton.setOnClickListener(v -> onAccessButton());
        binding.readouts.zeroButton.setOnClickListener(v -> viewModel.zeroAltitude());
        binding.readouts.zeroButton.setOnLongClickListener(v -> {
            viewModel.clearAltitudeZero();
            return true;
        });
        binding.readouts.soundButton.setOnClickListener(v -> toggleSound());
        binding.readouts.tuneButton.setOnClickListener(v -> new VarioSoundDialog().show(getChildFragmentManager(), SOUND_TAG));

        viewModel.getSnapshot().observe(getViewLifecycleOwner(), snapshot -> render());
        viewModel.getUnits().observe(getViewLifecycleOwner(), units -> render());
        viewModel.getSound().observe(getViewLifecycleOwner(), sound -> render());
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.setWatching(true);
    }

    @Override
    public void onPause() {
        viewModel.setWatching(false);
        super.onPause();
    }

    private void toggleSound() {
        VarioSoundSettings sound = viewModel.getSound().getValue();
        viewModel.saveSound(sound.withEnabled(!sound.isEnabled()));
    }

    private void onRecordButton() {
        FlightSnapshot snapshot = viewModel.getSnapshot().getValue();
        if (snapshot != null && snapshot.isRecording()) {
            requireContext().stopService(new Intent(requireContext(), FlightRecordingService.class));
        } else if (!GpsTracker.hasPermission(requireContext())) {
            permissionLauncher.launch(requestedPermissions());
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        if (!GpsTracker.isSwitchedOn(requireContext())) {
            Toast.makeText(requireContext(), R.string.flights_gps_off, Toast.LENGTH_LONG).show();
            return;
        }
        ContextCompat.startForegroundService(requireContext(), new Intent(requireContext(), FlightRecordingService.class));
    }

    private String[] requestedPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS};
        }
        return new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    private void onPermissionResult(Map<String, Boolean> result) {
        if (GpsTracker.hasPermission(requireContext())) {
            permissionBlocked = false;
            startRecording();
        } else {
            permissionBlocked = !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
            render();
        }
    }

    private void onAccessButton() {
        if (permissionBlocked) {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireContext().getPackageName(), null)));
        } else {
            permissionLauncher.launch(requestedPermissions());
        }
    }

    private void render() {
        FlightSnapshot snapshot = viewModel.getSnapshot().getValue();
        FlightUnits units = viewModel.getUnits().getValue();
        if (binding == null || snapshot == null || units == null) {
            return;
        }
        showInstruments(snapshot, units);
        showReadouts(snapshot, units);
        showStats(snapshot.getStats(), snapshot, units);
        showControls(snapshot);
    }

    private void showInstruments(FlightSnapshot snapshot, FlightUnits units) {
        Orientation orientation = snapshot.getOrientation();
        binding.instruments.compass.setHeading(orientation.getHeading());
        binding.instruments.horizon.setAttitude(orientation.getPitch(), orientation.getRoll());
        binding.instruments.vario.setUnit(units.getClimb());
        binding.instruments.vario.setClimbRate(snapshot.getVerticalSpeed());
    }

    private void showReadouts(FlightSnapshot snapshot, FlightUnits units) {
        binding.readouts.altitudeText.setText(String.format(Locale.US, "%.0f", units.getAltitude().fromMetres(snapshot.getAltitude())));
        binding.readouts.altitudeUnit.setText(units.getAltitude().symbol());
        binding.readouts.altitudeBasis.setText(snapshot.isAltitudeFromStart()
                ? R.string.flights_altitude_start : R.string.flights_altitude_sea);
        binding.readouts.speedText.setText(FlightFormat.speed(snapshot.getSpeed(), units.getSpeed()));
        binding.readouts.gText.setText(FlightFormat.gForce(snapshot.getGForce()));
        VarioSoundSettings sound = viewModel.getSound().getValue();
        binding.readouts.soundButton.setText(sound != null && sound.isEnabled()
                ? R.string.flights_sound_on : R.string.flights_sound_off);
    }

    private void showStats(FlightStats stats, FlightSnapshot snapshot, FlightUnits units) {
        binding.stats.flightTime.setText(FlightTime.format(stats.getDurationMillis()));
        binding.stats.distance.setText(FlightFormat.distance(stats.getDistanceMetres(), units.getSpeed()));
        binding.stats.maxSpeed.setText(FlightFormat.speed(stats.getMaxSpeed(), units.getSpeed()));
        binding.stats.averageSpeed.setText(FlightFormat.speed(stats.getAverageSpeed(), units.getSpeed()));
        binding.stats.maxAltitude.setText(FlightFormat.altitude(stats.getMaxAltitude(), units.getAltitude()));
        binding.stats.climbRate.setText(FlightFormat.climbRate(snapshot.getVerticalSpeed(), units.getClimb()));
        binding.stats.maxG.setText(FlightFormat.gForce(stats.getMaxGForce()));
    }

    private void showControls(FlightSnapshot snapshot) {
        binding.controls.recordButton.setText(recordLabel(snapshot.isRecording()));
        boolean needsPermission = !GpsTracker.hasPermission(requireContext()) && permissionBlocked;
        binding.controls.accessPanel.setVisibility(needsPermission ? View.VISIBLE : View.GONE);
        binding.controls.accessButton.setText(permissionBlocked ? R.string.flights_open_settings : R.string.flights_allow);
        announceWaitingForGps(snapshot.isRecording() && !snapshot.hasFix());
    }

    /** The label of the record button with a dot in front: dark red while idle, bright red while recording. */
    private CharSequence recordLabel(boolean recording) {
        SpannableString label = new SpannableString(RECORD_DOT + " "
                + getString(recording ? R.string.flights_stop : R.string.flights_start));
        int color = ContextCompat.getColor(requireContext(), recording ? R.color.record_active : R.color.record_idle);
        label.setSpan(new ForegroundColorSpan(color), 0, RECORD_DOT.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return label;
    }

    /** Tells once per wait, not on every update, that the GPS has no fix yet. */
    private void announceWaitingForGps(boolean waiting) {
        if (waiting && !waitingToastShown) {
            Toast.makeText(requireContext(), R.string.flights_status_waiting, Toast.LENGTH_LONG).show();
        }
        waitingToastShown = waiting;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // memory leak prevention
    }
}
