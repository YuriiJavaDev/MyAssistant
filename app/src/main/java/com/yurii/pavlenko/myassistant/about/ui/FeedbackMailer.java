package com.yurii.pavlenko.myassistant.about.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.yurii.pavlenko.myassistant.about.data.DeveloperContact;
import com.yurii.pavlenko.myassistant.about.logic.FeedbackMessage;

/** Hands the message to the email app of the user; nothing is sent until the user presses send there. */
final class FeedbackMailer {

    private FeedbackMailer() {
    }

    /** True when an email app opened with the message. */
    static boolean open(@NonNull Context context, @NonNull String text, boolean includeDetails) {
        FeedbackMessage.Details details = AppDetails.forFeedback(context);
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{DeveloperContact.EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, FeedbackMessage.subject(AppDetails.versionName(context)));
        intent.putExtra(Intent.EXTRA_TEXT, FeedbackMessage.body(text, includeDetails, details));
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }
}
