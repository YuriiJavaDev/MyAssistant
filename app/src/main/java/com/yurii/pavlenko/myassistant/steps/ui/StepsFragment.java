package com.yurii.pavlenko.myassistant.steps.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
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

import com.google.android.material.datepicker.MaterialDatePicker;
import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.databinding.FragmentStepsBinding;
import com.yurii.pavlenko.myassistant.steps.logic.DayTotals;
import com.yurii.pavlenko.myassistant.steps.logic.PeriodSummary;
import com.yurii.pavlenko.myassistant.steps.logic.StepLengths;
import com.yurii.pavlenko.myassistant.steps.sensor.StepSensor;
import com.yurii.pavlenko.myassistant.steps.viewmodel.PeriodStats;
import com.yurii.pavlenko.myassistant.steps.viewmodel.StepsViewModel;

import java.time.Instant;
import java.time.ZoneOffset;

/** The Steps tab: today's steps and distance, and statistics for a chosen period. */
public class StepsFragment extends Fragment {

    private static final long LIVE_INTERVAL_MILLIS = 30_000;
    private static final String SETTINGS_TAG = "step_settings";

    private FragmentStepsBinding binding;
    private StepsViewModel viewModel;
    private StepDayAdapter dayAdapter;
    private DayTotals todayTotals;
    private StepLengths lengths = StepLengths.defaults();
    private boolean permissionBlocked;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable liveTick = new Runnable() {
        @Override
        public void run() {
            viewModel.refreshToday();
            viewModel.recordNow();
            handler.postDelayed(this, LIVE_INTERVAL_MILLIS);
        }
    };

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                permissionBlocked = !granted
                        && !shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION);
                updateAccessPanel();
                if (granted) {
                    viewModel.recordNow();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStepsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StepsViewModel.class);

        dayAdapter = new StepDayAdapter();
        binding.dayRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.dayRecycler.setAdapter(dayAdapter);

        binding.accessButton.setOnClickListener(v -> onAccessButton());
        binding.last7Button.setOnClickListener(v -> viewModel.selectLastDays(7));
        binding.last30Button.setOnClickListener(v -> viewModel.selectLastDays(30));
        binding.monthButton.setOnClickListener(v -> viewModel.selectThisMonth());
        binding.pickDatesButton.setOnClickListener(v -> pickDates());
        binding.settingsButton.setOnClickListener(v -> new StepSettingsDialog().show(getChildFragmentManager(), SETTINGS_TAG));

        viewModel.getTodayTotals().observe(getViewLifecycleOwner(), totals -> {
            todayTotals = totals;
            renderToday();
        });
        viewModel.getLengths().observe(getViewLifecycleOwner(), saved -> {
            lengths = saved;
            renderToday();
        });
        viewModel.getPeriod().observe(getViewLifecycleOwner(), this::renderPeriod);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAccessPanel();
        handler.post(liveTick);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(liveTick);
        viewModel.recordNow();
    }

    private void updateAccessPanel() {
        if (binding == null) {
            return;
        }
        boolean hasSensor = StepSensor.isAvailable(requireContext());
        boolean hasPermission = StepSensor.hasPermission(requireContext());
        binding.accessPanel.setVisibility(hasSensor && hasPermission ? View.GONE : View.VISIBLE);
        binding.accessMessage.setText(hasSensor ? R.string.steps_permission_needed : R.string.steps_no_sensor);
        binding.accessButton.setVisibility(hasSensor ? View.VISIBLE : View.GONE);
        binding.accessButton.setText(permissionBlocked ? R.string.steps_open_settings : R.string.steps_allow);
    }

    private void onAccessButton() {
        if (permissionBlocked) {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireContext().getPackageName(), null)));
        } else {
            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
        }
    }

    private void pickDates() {
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(R.string.steps_pick_dates)
                .build();
        picker.addOnPositiveButtonClickListener(selection ->
                viewModel.selectRange(toEpochDay(selection.first), toEpochDay(selection.second)));
        picker.show(getChildFragmentManager(), "step_range");
    }

    /** The picker reports each chosen day as midnight UTC. */
    private static long toEpochDay(long pickerMillis) {
        return Instant.ofEpochMilli(pickerMillis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay();
    }

    private void renderToday() {
        int walk = todayTotals == null ? 0 : todayTotals.getWalkSteps();
        int run = todayTotals == null ? 0 : todayTotals.getRunSteps();
        binding.todaySteps.setText(StepFormat.steps(walk + run));
        binding.todayDistance.setText(StepFormat.distance(requireContext(), lengths.distanceMeters(walk, run)));
        binding.todaySplit.setText(getString(R.string.steps_walk_run, StepFormat.steps(walk), StepFormat.steps(run)));
        if (viewModel.getPeriod().getValue() != null) {
            renderPeriod(viewModel.getPeriod().getValue());
        }
    }

    private void renderPeriod(PeriodStats stats) {
        PeriodSummary summary = stats.getSummary();
        binding.periodRange.setText(getString(R.string.steps_range_format,
                StepFormat.date(stats.getFromEpochDay()), StepFormat.date(stats.getToEpochDay())));
        binding.statTotal.setText(StepFormat.steps(summary.totalSteps()));
        binding.statDistance.setText(StepFormat.distance(requireContext(), summary.getDistanceMeters()));
        binding.statAverage.setText(getString(R.string.steps_average_value,
                StepFormat.steps(summary.averageStepsPerDay()),
                StepFormat.distance(requireContext(), summary.averageDistanceMetersPerDay())));
        binding.statBest.setText(summary.getBestDaySteps() == 0 ? "–" : getString(R.string.steps_best_day_value,
                StepFormat.date(summary.getBestDayEpoch()), StepFormat.steps(summary.getBestDaySteps())));
        binding.statWalking.setText(StepFormat.steps(summary.getWalkSteps()));
        binding.statRunning.setText(StepFormat.steps(summary.getRunSteps()));
        binding.statActive.setText(getString(R.string.steps_active_days_value,
                summary.getDaysWithSteps(), summary.getDayCount()));
        binding.periodEmpty.setVisibility(stats.getDays().isEmpty() ? View.VISIBLE : View.GONE);
        dayAdapter.submit(stats.getDays(), lengths);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
