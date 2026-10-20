package com.yurii.pavlenko.myassistant.tasks.logic;

/** How much a task matters. The key is what the database stores; it is never translated. */
public enum Importance {
    NORMAL("Normal", 3),
    IMPORTANT("Important", 2),
    URGENT("Urgent", 1);

    public final String key;
    /** Sort position: the lower the rank, the sooner the task is listed. */
    public final int rank;

    Importance(String key, int rank) {
        this.key = key;
        this.rank = rank;
    }

    /** The importance behind a stored key; Normal for a missing or unknown one. */
    public static Importance fromKey(String key) {
        for (Importance importance : values()) {
            if (importance.key.equalsIgnoreCase(key)) {
                return importance;
            }
        }
        return NORMAL;
    }
}
