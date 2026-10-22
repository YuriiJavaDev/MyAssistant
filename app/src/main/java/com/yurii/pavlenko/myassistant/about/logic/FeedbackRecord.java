package com.yurii.pavlenko.myassistant.about.logic;

import androidx.annotation.NonNull;

/** A message the user has sent: when, and its normalized words for the check for repeats. */
public final class FeedbackRecord {

    private final long sentAtMillis;
    private final String normalizedText;

    public FeedbackRecord(long sentAtMillis, @NonNull String normalizedText) {
        this.sentAtMillis = sentAtMillis;
        this.normalizedText = normalizedText;
    }

    public long getSentAtMillis() {
        return sentAtMillis;
    }

    @NonNull
    public String getNormalizedText() {
        return normalizedText;
    }
}
