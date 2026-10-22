package com.yurii.pavlenko.myassistant.ui.locale;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

/** The language chosen for the app, for the places that are not an activity: services, speech and reports. */
public final class AppLocale {

    private AppLocale() {
    }

    /** The language chosen in the app, otherwise the language of the device. */
    @NonNull
    public static Locale current() {
        LocaleListCompat appLocales = AppCompatDelegate.getApplicationLocales();
        Locale locale = appLocales.isEmpty() ? null : appLocales.get(0);
        return locale != null ? locale : Locale.getDefault();
    }

    /** A context whose resources are in the language of the app, whatever the system language is. */
    @NonNull
    public static Context localized(@NonNull Context context) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(current());
        return context.createConfigurationContext(configuration);
    }
}
