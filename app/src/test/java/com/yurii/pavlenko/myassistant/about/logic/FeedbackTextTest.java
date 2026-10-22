package com.yurii.pavlenko.myassistant.about.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FeedbackTextTest {

    @Test
    public void normalizeKeepsLowerCaseWordsOnly() {
        assertEquals("hello world 2", FeedbackText.normalize("  Hello,   WORLD!!! 2 "));
        assertEquals("", FeedbackText.normalize("?!... "));
    }

    @Test
    public void normalizeKeepsOtherAlphabets() {
        assertEquals("сканер працює", FeedbackText.normalize("Сканер — працює!"));
    }

    @Test
    public void wordCountCountsSeparatedWords() {
        assertEquals(0, FeedbackText.wordCount(""));
        assertEquals(3, FeedbackText.wordCount("a bb ccc"));
    }

    @Test
    public void similarityIsSharedWordsOverAllWords() {
        assertEquals(1.0, FeedbackText.similarity("a b c", "a b c"), 0);
        assertEquals(0.5, FeedbackText.similarity("a b c", "a b d"), 1e-9);
        assertEquals(0.0, FeedbackText.similarity("a b", "c d"), 0);
        assertEquals(0.0, FeedbackText.similarity("", "c d"), 0);
    }
}
