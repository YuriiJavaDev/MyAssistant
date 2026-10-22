package com.yurii.pavlenko.myassistant.about.ui;

import android.content.Context;

import androidx.annotation.NonNull;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.about.logic.FeedbackCheck;

/** The text that tells the user why a message cannot be sent now. */
final class FeedbackErrorText {

    private static final long MINUTE_MILLIS = 60_000L;

    private FeedbackErrorText() {
    }

    @NonNull
    static String of(@NonNull Context context, @NonNull FeedbackCheck check) {
        switch (check.getVerdict()) {
            case TOO_SHORT:
                return context.getString(R.string.feedback_too_short);
            case DUPLICATE:
                return context.getString(R.string.feedback_duplicate);
            case WAIT:
                return context.getString(R.string.feedback_wait_minutes, minutesToWait(check));
            case DAILY_LIMIT:
                return context.getString(R.string.feedback_daily_limit);
            case MONTHLY_LIMIT:
                return context.getString(R.string.feedback_monthly_limit);
            default:
                return "";
        }
    }

    private static long minutesToWait(FeedbackCheck check) {
        return (check.getRetryAfterMillis() + MINUTE_MILLIS - 1) / MINUTE_MILLIS;
    }
}
