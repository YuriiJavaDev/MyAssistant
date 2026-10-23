package com.yurii.pavlenko.myassistant.about.logic;

import androidx.annotation.NonNull;

/** One piece of an information page: a heading, a paragraph or an item of a list. */
public final class InfoBlock {

    public enum Type {
        HEADING,
        PARAGRAPH,
        BULLET
    }

    private final Type type;
    private final String text;

    public InfoBlock(@NonNull Type type, @NonNull String text) {
        this.type = type;
        this.text = text;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    @NonNull
    public String getText() {
        return text;
    }
}
