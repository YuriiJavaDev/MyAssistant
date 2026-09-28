package com.yurii.pavlenko.myassistant;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.ExactAlarmPermissionHelper;
import com.yurii.pavlenko.myassistant.tasks.ui.handlers.TaskPreferences;

/**
 * Bottom sheet dialog fragment for managing reminders, alarm settings,
 * and exact alarm permissions.
 */
public class AlarmSettingsBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the alarm settings layout from XML resource
        View view = inflater.inflate(R.layout.dialog_alarm_settings, container, false);

        SwitchMaterial switchVoice = view.findViewById(R.id.switchVoiceAlert);
        SwitchMaterial switchFullScreen = view.findViewById(R.id.switchFullScreen);
        MaterialButton btnPermission = view.findViewById(R.id.btnExactAlarmPermission);

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
}