package com.yurii.pavlenko.myassistant.tasks.logic;

/** How much a task matters. The label is what the task list shows and what the database stores. */
public enum Importance {
    NORMAL("Normal", 3),
    IMPORTANT("Important", 2),
    URGENT("Urgent", 1);

    public final String label;
    /** Sort position: the lower the rank, the sooner the task is listed. */
    public final int rank;

    Importance(String label, int rank) {
        this.label = label;
        this.rank = rank;
    }

    /** The importance behind a stored label; Normal for a missing or unknown one. */
    public static Importance fromLabel(String label) {
        for (Importance importance : values()) {
            if (importance.label.equalsIgnoreCase(label)) {
                return importance;
            }
        }
        return NORMAL;
    }

    public static String[] labels() {
        Importance[] all = values();
        String[] labels = new String[all.length];
        for (int i = 0; i < all.length; i++) {
            labels[i] = all[i].label;
        }
        return labels;
    }
}
