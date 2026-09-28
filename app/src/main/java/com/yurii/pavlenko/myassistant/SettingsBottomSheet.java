package com.yurii.pavlenko.myassistant;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.ExactAlarmPermissionHelper;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskPreferences;

/**
 * Bottom sheet dialog fragment for managing application settings,
 * including alarms, display modes, design variations, and system permissions.
 */
public class SettingsBottomSheet extends BottomSheetDialogFragment {

    private AlertDialog alarmSettingsDialog = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the bottom sheet layout from XML resource
        View view = inflater.inflate(R.layout.dialog_settings, container, false);

        // Initialize alarm settings block click listener
        LinearLayout layoutAlarmSettings = view.findViewById(R.id.layoutAlarmSettings);
        layoutAlarmSettings.setOnClickListener(v -> {
            showAlarmSettingsDialog();
        });

        // Initialize display mode settings block click listener
        LinearLayout layoutDisplaySettings = view.findViewById(R.id.layoutDisplaySettings);
        layoutDisplaySettings.setOnClickListener(v -> {
            showThemeSelectionDialog();
        });

        // Initialize design customization block click listener
        LinearLayout layoutDesignSettings = view.findViewById(R.id.layoutDesignSettings);
        layoutDesignSettings.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Opening design customization", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        // Initialize system permissions block click listener
        LinearLayout layoutSystemPermissions = view.findViewById(R.id.layoutSystemPermissions);
        layoutSystemPermissions.setOnClickListener(v -> {
            openSystemAppSettings();
            dismiss();
        });

        return view;
    }

    /**
     * Displays a custom-styled dialog to manage alarm preferences and exact alarm permissions.
     */
    private void showAlarmSettingsDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_alarm_settings, null);

        alarmSettingsDialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (alarmSettingsDialog.getWindow() != null) {
            alarmSettingsDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        SwitchMaterial switchVoice = dialogView.findViewById(R.id.switchVoiceAlert);
        SwitchMaterial switchFullScreen = dialogView.findViewById(R.id.switchFullScreen);
        MaterialButton btnPermission = dialogView.findViewById(R.id.btnExactAlarmPermission);

        // Load current preference states
        switchVoice.setChecked(TaskPreferences.isVoiceEnabled(requireContext()));
        switchFullScreen.setChecked(TaskPreferences.isFullScreenEnabled(requireContext()));

        // Save voice setting changes
        switchVoice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            TaskPreferences.setVoiceEnabled(requireContext(), isChecked);
        });

        // Save full-screen setting changes
        switchFullScreen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            TaskPreferences.setFullScreenEnabled(requireContext(), isChecked);
        });

        // Handle exact alarm permission request
        btnPermission.setOnClickListener(v -> {
            if (!ExactAlarmPermissionHelper.hasExactAlarmPermission(requireContext())) {
                ExactAlarmPermissionHelper.requestExactAlarmPermission(requireContext());
            } else {
                Toast.makeText(requireContext(), "Exact alarm permission is already granted", Toast.LENGTH_SHORT).show();
            }
        });

        // Clear reference when dialog is dismissed
        alarmSettingsDialog.setOnDismissListener(dialog -> alarmSettingsDialog = null);

        alarmSettingsDialog.show();
    }

    /**
     * Displays a custom-styled dialog to select the application theme mode (Day/Night/System).
     */
    private void showThemeSelectionDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_display_mode, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        android.widget.RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroupDisplayMode);
        int currentTheme = AppCompatDelegate.getDefaultNightMode();

        if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            radioGroup.check(R.id.radioLight);
        } else if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            radioGroup.check(R.id.radioDark);
        } else {
            radioGroup.check(R.id.radioSystem);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioLight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.radioDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
            dialog.dismiss();
            dismiss();
        });

        dialog.show();
    }

    /**
     * Opens the system application details settings screen for managing permissions.
     */
    private void openSystemAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Dismiss the nested alarm dialog if it's showing to prevent window leaks on orientation change
        if (alarmSettingsDialog != null && alarmSettingsDialog.isShowing()) {
            alarmSettingsDialog.dismiss();
            alarmSettingsDialog = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Force dismiss the inner alarm dialog on rotation or when fragment stops to prevent UI freezing
        if (alarmSettingsDialog != null && alarmSettingsDialog.isShowing()) {
            alarmSettingsDialog.dismiss();
            alarmSettingsDialog = null;
        }
    }
}