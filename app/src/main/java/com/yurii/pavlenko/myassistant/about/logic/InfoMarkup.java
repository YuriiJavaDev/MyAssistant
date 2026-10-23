package com.yurii.pavlenko.myassistant.about.logic;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads the texts of an information page from the items of a string array: "# " starts a heading,
 * "- " starts a list item, anything else is a paragraph. Translations keep the same marks.
 */
public final class InfoMarkup {

    private static final String HEADING_MARK = "# ";
    private static final String BULLET_MARK = "- ";

    private InfoMarkup() {
    }

    @NonNull
    public static List<InfoBlock> parse(@NonNull String[] items) {
        List<InfoBlock> blocks = new ArrayList<>(items.length);
        for (String item : items) {
            blocks.add(parseItem(item.trim()));
        }
        return blocks;
    }

    @NonNull
    public static InfoBlock parseItem(@NonNull String item) {
        if (item.startsWith(HEADING_MARK)) {
            return new InfoBlock(InfoBlock.Type.HEADING, item.substring(HEADING_MARK.length()).trim());
        }
        if (item.startsWith(BULLET_MARK)) {
            return new InfoBlock(InfoBlock.Type.BULLET, item.substring(BULLET_MARK.length()).trim());
        }
        return new InfoBlock(InfoBlock.Type.PARAGRAPH, item);
    }

    /** The kind of an item by its mark, which a translation must not change. */
    @NonNull
    public static InfoBlock.Type typeOf(@NonNull String item) {
        return parseItem(item.trim()).getType();
    }
}
