package com.yurii.pavlenko.myassistant.about.ui;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.pm.PackageInfoCompat;

import com.yurii.pavlenko.myassistant.about.logic.FeedbackMessage;
import com.yurii.pavlenko.myassistant.ui.locale.AppLocale;

/** Reads the version of the app and the details of the phone that help to find a problem. */
final class AppDetails {

    private static final String UNKNOWN_VERSION = "?";

    private AppDetails() {
    }

    @NonNull
    static String versionName(@NonNull Context context) {
        PackageInfo info = packageInfo(context);
        return info == null || info.versionName == null ? UNKNOWN_VERSION : info.versionName;
    }

    @NonNull
    static FeedbackMessage.Details forFeedback(@NonNull Context context) {
        PackageInfo info = packageInfo(context);
        int build = info == null ? 0 : (int) PackageInfoCompat.getLongVersionCode(info);
        return new FeedbackMessage.Details(versionName(context), build, Build.VERSION.RELEASE, Build.VERSION.SDK_INT,
                Build.MANUFACTURER + " " + Build.MODEL, AppLocale.current().toLanguageTag());
    }

    private static PackageInfo packageInfo(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
