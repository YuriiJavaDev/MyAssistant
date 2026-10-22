package com.yurii.pavlenko.myassistant.ui.locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

/** Remembers which language the user chose for the app; AppCompat keeps it and restarts the screens. */
public final class LanguageStore {

    private LanguageStore() {
    }

    /** The tag of the language chosen in the app, or null while the app follows the language of the device. */
    @Nullable
    public static String chosenTag() {
        LocaleListCompat chosen = AppCompatDelegate.getApplicationLocales();
        if (chosen.isEmpty() || chosen.get(0) == null) {
            return null;
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
        return null;
    }

    /** Chooses a language, or null to follow the device. */
    public static void choose(@Nullable String tag) {
        AppCompatDelegate.setApplicationLocales(tag == null
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(tag));
    }
}
