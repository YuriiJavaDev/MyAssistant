package com.yurii.pavlenko.myassistant.about.logic;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Reduces a message to its words, so that the same text typed twice with other punctuation or case looks the same. */
public final class FeedbackText {

    private FeedbackText() {
    }

    /** The lower-case letters and digits of the text, the words separated by single spaces. */
    @NonNull
    public static String normalize(@NonNull String text) {
        StringBuilder result = new StringBuilder(text.length());
        boolean pendingSpace = false;
        String lower = text.toLowerCase(Locale.ROOT);
        for (int i = 0; i < lower.length(); ) {
            int codePoint = lower.codePointAt(i);
            i += Character.charCount(codePoint);
            if (Character.isLetterOrDigit(codePoint)) {
                if (pendingSpace && result.length() > 0) {
                    result.append(' ');
                }
                pendingSpace = false;
                result.appendCodePoint(codePoint);
            } else {
                pendingSpace = true;
            }
        }
        return result.toString();
    }

    /** The number of words in an already normalized text. */
    public static int wordCount(@NonNull String normalized) {
        return normalized.isEmpty() ? 0 : normalized.split(" ").length;
    }

    /** How much two normalized texts share, from 0 (nothing) to 1 (the same words), by their sets of words. */
    public static double similarity(@NonNull String first, @NonNull String second) {
        if (first.equals(second)) {
            return 1;
        }
        Set<String> a = words(first);
        Set<String> b = words(second);
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        Set<String> common = new HashSet<>(a);
        common.retainAll(b);
        Set<String> all = new HashSet<>(a);
        all.addAll(b);
        return (double) common.size() / all.size();
    }

    private static Set<String> words(String normalized) {
        return normalized.isEmpty() ? new HashSet<>() : new HashSet<>(Arrays.asList(normalized.split(" ")));
    }
}
