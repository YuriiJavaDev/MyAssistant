package com.yurii.pavlenko.myassistant;

import android.app.Dialog;
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

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Bottom sheet dialog fragment for managing application settings,
 * including alarms, display modes, design variations, and system permissions.
 */
public class SettingsBottomSheet extends BottomSheetDialogFragment {

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
            showDesignSelectionDialog();
        });

        // Initialize system permissions block click listener
        LinearLayout layoutSystemPermissions = view.findViewById(R.id.layoutSystemPermissions);
        layoutSystemPermissions.setOnClickListener(v -> {
            openSystemAppSettings();
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Force the bottom sheet to expand fully in landscape mode so no rows are clipped
        Dialog dialog = getDialog();
        if (dialog instanceof BottomSheetDialog) {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
            View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        }
    }

    /**
     * Displays the bottom sheet dialog to manage alarm preferences and exact alarm permissions.
     */
    private void showAlarmSettingsDialog() {
        AlarmSettingsBottomSheet alarmSettingsBottomSheet = new AlarmSettingsBottomSheet();
        alarmSettingsBottomSheet.show(getParentFragmentManager(), "AlarmSettingsBottomSheet");
    }

    /**
     * Displays the bottom sheet dialog to select the application theme mode (Day/Night/System).
     */
    private void showThemeSelectionDialog() {
        ThemeSelectionBottomSheet themeSelectionBottomSheet = new ThemeSelectionBottomSheet();
        themeSelectionBottomSheet.show(getParentFragmentManager(), "ThemeSelectionBottomSheet");
    }

    /**
     * Displays the bottom sheet dialog to select application UI design variations.
     */
    private void showDesignSelectionDialog() {
        DesignSelectionBottomSheet designSelectionBottomSheet = new DesignSelectionBottomSheet();
        designSelectionBottomSheet.show(getParentFragmentManager(), "DesignSelectionBottomSheet");
    }

    /**
     * Opens the system application details settings screen for managing permissions.
     */
    private void openSystemAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
        startActivity(intent);
    }
}