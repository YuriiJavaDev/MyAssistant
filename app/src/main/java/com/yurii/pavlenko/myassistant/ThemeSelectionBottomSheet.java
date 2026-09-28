package com.yurii.pavlenko.myassistant;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Bottom sheet dialog fragment for selecting the application theme mode (Day/Night/System).
 */
public class ThemeSelectionBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the display mode selection layout from XML resource
        View view = inflater.inflate(R.layout.dialog_display_mode, container, false);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroupDisplayMode);
        int currentTheme = AppCompatDelegate.getDefaultNightMode();

        // Check the radio button corresponding to the current theme mode
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            radioGroup.check(R.id.radioLight);
        } else if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            radioGroup.check(R.id.radioDark);
        } else {
            radioGroup.check(R.id.radioSystem);
        }

        // Handle theme selection changes and apply the chosen mode
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioLight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (checkedId == R.id.radioDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }

            // Dismiss the theme selection bottom sheet
            dismiss();

            // Also dismiss the parent settings bottom sheet to return directly to the main screen
            Fragment settingsSheet = getParentFragmentManager().findFragmentByTag("SettingsBottomSheet");
            if (settingsSheet instanceof BottomSheetDialogFragment) {
                ((BottomSheetDialogFragment) settingsSheet).dismiss();
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