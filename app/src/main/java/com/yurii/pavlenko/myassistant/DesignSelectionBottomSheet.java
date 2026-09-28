package com.yurii.pavlenko.myassistant;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Bottom sheet dialog fragment for selecting application UI design variations.
 */
public class DesignSelectionBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the app design selection layout from XML resource
        View view = inflater.inflate(R.layout.dialog_app_design, container, false);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroupAppDesign);
        // MyAssistant 1 is checked by default
        radioGroup.check(R.id.radioDesign1);

        // Handle design selection changes
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioDesign1) {
                Toast.makeText(requireContext(), "MyAssistant 1 selected", Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.radioDesign2) {
                Toast.makeText(requireContext(), "MyAssistant 2 is coming soon", Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.radioDesign3) {
                Toast.makeText(requireContext(), "MyAssistant 3 is coming soon", Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.radioDesign4) {
                Toast.makeText(requireContext(), "MyAssistant 4 is coming soon", Toast.LENGTH_SHORT).show();
            }

            // Dismiss the design selection bottom sheet
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