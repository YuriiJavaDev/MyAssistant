package com.yurii.pavlenko.myassistant.ui.locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

/** Remembers which language the user chose for the app, English until then; AppCompat keeps it and restarts the screens. */
public final class LanguageStore {

    private static final String DEFAULT_TAG = "en";

    private LanguageStore() {
    }

    /** Makes English the language of the app on the first launch instead of the language of the device. */
    public static void ensureChosen() {
        LocaleListCompat chosen = AppCompatDelegate.getApplicationLocales();
        if (chosen.isEmpty()) {
            choose(DEFAULT_TAG);
        }
    }

    /** The tag of the language chosen in the app. */
    @NonNull
    public static String chosenTag() {
        LocaleListCompat chosen = AppCompatDelegate.getApplicationLocales();
        if (chosen.isEmpty() || chosen.get(0) == null) {
            return DEFAULT_TAG;
        }
        String language = chosen.get(0).getLanguage();
        String tag = chosen.get(0).toLanguageTag();
        for (AppLanguage candidate : AppLanguage.all()) {
            if (candidate.getTag().equalsIgnoreCase(tag)) {
                return candidate.getTag();
            }
        }
        for (AppLanguage candidate : AppLanguage.all()) {
            if (candidate.getTag().equalsIgnoreCase(language)) {
                return candidate.getTag();
            }
        }
        return DEFAULT_TAG;
    }

    /** Chooses the language of the app. */
    public static void choose(@NonNull String tag) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));
    }
}
