package com.yurii.pavlenko.myassistant.backup;

import java.util.Collections;
import java.util.List;

/** What a finished cloud upload or restore achieved. */
public final class SyncResult {

    public enum Kind {
        UPLOADED,
        RESTORED,
        RESTORED_PARTLY
    }

    /** A kind of data that was not found in the cloud. */
    public enum Part {
        STEPS,
        FLIGHTS,
        SCANS
    }

    private final Kind kind;
    private final List<Part> missing;

    private SyncResult(Kind kind, List<Part> missing) {
        this.kind = kind;
        this.missing = missing;
    }

    public static SyncResult uploaded() {
        return new SyncResult(Kind.UPLOADED, Collections.<Part>emptyList());
    }

    /** Everything was restored when nothing is missing, otherwise only the tasks and what is not in the list. */
    public static SyncResult restored(List<Part> missing) {
        return new SyncResult(missing.isEmpty() ? Kind.RESTORED : Kind.RESTORED_PARTLY, missing);
    }

    public Kind getKind() {
        return kind;
    }

    public List<Part> getMissing() {
        return missing;
    }
}
