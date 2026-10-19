package com.yurii.pavlenko.myassistant;

import android.app.Dialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.yurii.pavlenko.myassistant.ui.theme.AppDesign;
import com.yurii.pavlenko.myassistant.ui.theme.DesignStore;
import com.yurii.pavlenko.myassistant.ui.theme.DesignSwatches;
import com.yurii.pavlenko.myassistant.ui.theme.ThemeColors;

/**
 * Bottom sheet dialog fragment for choosing the look of the application.
 */
public class DesignSelectionBottomSheet extends BottomSheetDialogFragment {

    private static final int SWATCH_SIZE_DP = 16;
    private static final int SWATCH_GAP_DP = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_app_design, container, false);
        LinearLayout list = view.findViewById(R.id.designList);
        AppDesign current = DesignStore.get(requireContext());

        for (AppDesign design : AppDesign.values()) {
            list.addView(createOption(inflater, list, design, design == current));
        }
        return view;
    }

    private View createOption(LayoutInflater inflater, ViewGroup parent, AppDesign design, boolean selected) {
        View option = inflater.inflate(R.layout.item_design_option, parent, false);
        ((RadioButton) option.findViewById(R.id.designRadio)).setChecked(selected);
        ((TextView) option.findViewById(R.id.designName)).setText(design.nameRes);
        ((TextView) option.findViewById(R.id.designAbout)).setText(design.aboutRes);
        addSwatches(option.findViewById(R.id.designSwatches), design);
        option.setOnClickListener(v -> choose(design));
        return option;
    }

    private void addSwatches(LinearLayout row, AppDesign design) {
        float density = getResources().getDisplayMetrics().density;
        int size = Math.round(SWATCH_SIZE_DP * density);
        int gap = Math.round(SWATCH_GAP_DP * density);
        int rim = ThemeColors.get(requireContext(), R.attr.maOutline);

        for (int colour : DesignSwatches.of(requireContext(), design)) {
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(colour);
            dot.setStroke(Math.round(density), rim);

            View swatch = new View(requireContext());
            swatch.setBackground(dot);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMarginStart(gap);
            row.addView(swatch, params);
        }
    }

    private void choose(AppDesign design) {
        FragmentActivity activity = requireActivity();
        DesignStore.set(activity, design);

        dismiss();

        // Also dismiss the parent settings bottom sheet to return directly to the main screen
        Fragment settingsSheet = getParentFragmentManager().findFragmentByTag("SettingsBottomSheet");
        if (settingsSheet instanceof BottomSheetDialogFragment) {
            ((BottomSheetDialogFragment) settingsSheet).dismiss();
        }

        activity.recreate();
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
