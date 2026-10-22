package com.yurii.pavlenko.myassistant.about.logic;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Keeps the feedback mailbox from being flooded: a message needs a few words, may not repeat an earlier one,
 * and only a few messages may be sent a day and a month. The limits are the same for every kind of sender.
 */
public final class FeedbackLimiter {

    public static final int MIN_LENGTH = 10;
    public static final int MIN_WORDS = 2;
    public static final int MAX_LENGTH = 2000;
    public static final long MIN_GAP_MILLIS = 2 * 60 * 1000L;
    public static final long DAY_MILLIS = 24 * 60 * 60 * 1000L;
    public static final long MONTH_MILLIS = 30 * DAY_MILLIS;
    public static final int MAX_PER_DAY = 3;
    public static final int MAX_PER_MONTH = 10;
    public static final double SAME_MESSAGE_SIMILARITY = 0.8;

    private FeedbackLimiter() {
    }

    /** Decides whether the text may be sent now, given the messages sent before, in any order. */
    @NonNull
    public static FeedbackCheck check(@NonNull String text, long nowMillis, @NonNull List<FeedbackRecord> history) {
        String normalized = FeedbackText.normalize(text);
        if (normalized.length() < MIN_LENGTH || FeedbackText.wordCount(normalized) < MIN_WORDS) {
            return FeedbackCheck.of(FeedbackCheck.Verdict.TOO_SHORT);
        }
        if (repeatsEarlierMessage(normalized, history)) {
            return FeedbackCheck.of(FeedbackCheck.Verdict.DUPLICATE);
        }
        if (!history.isEmpty()) {
            long sinceLast = Math.max(nowMillis - latestSentAt(history), 0);
            if (sinceLast < MIN_GAP_MILLIS) {
                return FeedbackCheck.of(FeedbackCheck.Verdict.WAIT, MIN_GAP_MILLIS - sinceLast);
            }
        }
        long dayWait = waitForRoom(history, nowMillis, DAY_MILLIS, MAX_PER_DAY);
        if (dayWait > 0) {
            return FeedbackCheck.of(FeedbackCheck.Verdict.DAILY_LIMIT, dayWait);
        }
        long monthWait = waitForRoom(history, nowMillis, MONTH_MILLIS, MAX_PER_MONTH);
        if (monthWait > 0) {
            return FeedbackCheck.of(FeedbackCheck.Verdict.MONTHLY_LIMIT, monthWait);
        }
        return FeedbackCheck.of(FeedbackCheck.Verdict.ALLOWED);
    }

    private static boolean repeatsEarlierMessage(String normalized, List<FeedbackRecord> history) {
        for (FeedbackRecord record : history) {
            if (FeedbackText.similarity(normalized, record.getNormalizedText()) >= SAME_MESSAGE_SIMILARITY) {
                return true;
            }
        }
        return false;
    }

    private static long latestSentAt(List<FeedbackRecord> history) {
        long latest = Long.MIN_VALUE;
        for (FeedbackRecord record : history) {
            latest = Math.max(latest, record.getSentAtMillis());
        }
        return latest;
    }

    /** 0 when another message fits into the period, otherwise the time until the oldest one in it leaves it. */
    private static long waitForRoom(List<FeedbackRecord> history, long nowMillis, long periodMillis, int maxInPeriod) {
        int inPeriod = 0;
        long oldest = Long.MAX_VALUE;
        for (FeedbackRecord record : history) {
            long age = nowMillis - record.getSentAtMillis();
            if (age < periodMillis) {
                inPeriod++;
                oldest = Math.min(oldest, record.getSentAtMillis());
            }
        }
        if (inPeriod < maxInPeriod) {
            return 0;
        }
        return Math.max(oldest + periodMillis - nowMillis, 1);
    }
}
