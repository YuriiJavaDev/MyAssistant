package com.yurii.pavlenko.myassistant.about.logic;

import androidx.annotation.NonNull;

/** The answer to "may this message be sent now", with how long to wait when waiting helps. */
public final class FeedbackCheck {

    public enum Verdict {
        ALLOWED,
        TOO_SHORT,
        DUPLICATE,
        WAIT,
        DAILY_LIMIT,
        MONTHLY_LIMIT
    }

    private final Verdict verdict;
    private final long retryAfterMillis;

    private FeedbackCheck(Verdict verdict, long retryAfterMillis) {
        this.verdict = verdict;
        this.retryAfterMillis = retryAfterMillis;
    }

    @NonNull
    public static FeedbackCheck of(@NonNull Verdict verdict) {
        return new FeedbackCheck(verdict, 0);
    }

    @NonNull
    public static FeedbackCheck of(@NonNull Verdict verdict, long retryAfterMillis) {
        return new FeedbackCheck(verdict, retryAfterMillis);
    }

    @NonNull
    public Verdict getVerdict() {
        return verdict;
    }

    public boolean isAllowed() {
        return verdict == Verdict.ALLOWED;
    }

    /** Milliseconds until the message could be sent, 0 when there is nothing to wait for. */
    public long getRetryAfterMillis() {
        return retryAfterMillis;
    }
}
