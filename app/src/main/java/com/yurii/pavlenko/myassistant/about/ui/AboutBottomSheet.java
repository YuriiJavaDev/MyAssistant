package com.yurii.pavlenko.myassistant.about.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.about.data.DeveloperContact;
import com.yurii.pavlenko.myassistant.databinding.DialogAboutBinding;

/** What the app is, which version it is, who made it, where its data comes from and where to read more. */
public class AboutBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DialogAboutBinding binding = DialogAboutBinding.inflate(inflater, container, false);
        binding.tvAboutVersion.setText(getString(R.string.about_version_format, AppDetails.versionName(requireContext())));
        binding.btnAboutReadme.setOnClickListener(v -> AppLinks.open(requireContext(), DeveloperContact.README_URL));
        binding.btnAboutLicense.setOnClickListener(v -> AppLinks.open(requireContext(), DeveloperContact.LICENSE_URL));
        binding.btnAboutPrivacy.setOnClickListener(v -> AppLinks.open(requireContext(), DeveloperContact.PRIVACY_URL));
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Force the bottom sheet to expand fully in landscape mode so no rows are clipped
        Dialog dialog = getDialog();
        if (dialog instanceof BottomSheetDialog) {
            View bottomSheet = ((BottomSheetDialog) dialog).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        }
    }
}
