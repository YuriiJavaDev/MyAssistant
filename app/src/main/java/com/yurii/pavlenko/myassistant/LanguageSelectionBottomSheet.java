package com.yurii.pavlenko.myassistant;

import android.app.Dialog;
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

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.yurii.pavlenko.myassistant.ui.locale.AppLanguage;
import com.yurii.pavlenko.myassistant.ui.locale.LanguageStore;

/** Bottom sheet for choosing the language of the application, or following the language of the device. */
public class LanguageSelectionBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_language, container, false);
        LinearLayout list = view.findViewById(R.id.languageList);
        String chosen = LanguageStore.chosenTag();

        list.addView(createOption(inflater, list, getString(R.string.language_system_default), null, chosen == null));
        for (AppLanguage language : AppLanguage.all()) {
            list.addView(createOption(inflater, list, language.getNativeName(), language.getTag(),
                    language.getTag().equals(chosen)));
        }
        return view;
    }

    private View createOption(LayoutInflater inflater, ViewGroup parent, String name, @Nullable String tag, boolean selected) {
        View option = inflater.inflate(R.layout.item_language_option, parent, false);
        ((RadioButton) option.findViewById(R.id.languageRadio)).setChecked(selected);
        ((TextView) option.findViewById(R.id.languageName)).setText(name);
        option.setOnClickListener(v -> choose(tag));
        return option;
    }

    private void choose(@Nullable String tag) {
        dismiss();
        Fragment settingsSheet = getParentFragmentManager().findFragmentByTag("SettingsBottomSheet");
        if (settingsSheet instanceof BottomSheetDialogFragment) {
            ((BottomSheetDialogFragment) settingsSheet).dismiss();
        }
        // The screens restart in the new language by themselves
        LanguageStore.choose(tag);
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
