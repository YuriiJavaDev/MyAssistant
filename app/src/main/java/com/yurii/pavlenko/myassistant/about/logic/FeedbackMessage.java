package com.yurii.pavlenko.myassistant.about.logic;

import androidx.annotation.NonNull;

/** The email to the developer: a fixed subject and the user's text, optionally with the technical details of the phone. */
public final class FeedbackMessage {

    private static final String SUBJECT_PREFIX = "[MyAssistant] Feedback ";

    private FeedbackMessage() {
    }

    @NonNull
    public static String subject(@NonNull String appVersion) {
        return SUBJECT_PREFIX + appVersion;
    }

    /** The text with the details under a dashed line; the details are in English because the developer reads them. */
    @NonNull
    public static String body(@NonNull String userText, boolean includeDetails, @NonNull Details details) {
        String text = userText.trim();
        if (!includeDetails) {
            return text;
        }
        return text + "\n\n---\n"
                + "App: " + details.appVersion + " (" + details.appBuild + ")\n"
                + "Android: " + details.androidRelease + " (API " + details.androidApi + ")\n"
                + "Phone: " + details.phone + "\n"
                + "Language: " + details.language;
    }

    /** The technical details that help to find a problem. */
    public static final class Details {
        final String appVersion;
        final int appBuild;
        final String androidRelease;
        final int androidApi;
        final String phone;
        final String language;

        public Details(@NonNull String appVersion, int appBuild, @NonNull String androidRelease, int androidApi,
                       @NonNull String phone, @NonNull String language) {
            this.appVersion = appVersion;
            this.appBuild = appBuild;
            this.androidRelease = androidRelease;
            this.androidApi = androidApi;
            this.phone = phone;
            this.language = language;
        }
    }
}
