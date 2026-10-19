package com.yurii.pavlenko.myassistant.flights.logic;

/** How many flight reports may be kept, and how many are in the journal now. */
public final class ReportQuota {

    public static final int NO_LIMIT = 0;
    public static final int DEFAULT_LIMIT = 100;
    public static final int MAX_LIMIT = 9999;
    /** The user is warned when this many reports or fewer can still be added. */
    public static final int WARNING_REMAINING = 10;

    private final int limit;
    private final int used;

    public ReportQuota(int limit, int used) {
        this.limit = limit;
        this.used = used;
    }

    /** Keeps a typed limit within what is allowed; zero means no limit. */
    public static int sanitize(int limit) {
        return Math.max(NO_LIMIT, Math.min(limit, MAX_LIMIT));
    }

    public int getLimit() {
        return limit;
    }

    public boolean isFull() {
        return limit != NO_LIMIT && used >= limit;
    }

    /** How many more reports fit; meaningful only when there is a limit. */
    public int remaining() {
        return Math.max(0, limit - used);
    }

    /** True from the moment only a few places are left until the limit is reached. */
    public boolean isRunningOut() {
        return limit != NO_LIMIT && !isFull() && remaining() <= WARNING_REMAINING;
    }
}
