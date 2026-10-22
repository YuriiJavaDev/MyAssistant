package com.yurii.pavlenko.myassistant.about.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FeedbackLimiterTest {

    private static final long MINUTE = 60_000L;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;
    private static final long NOW = 1_000 * DAY;

    private static String message(int number) {
        return "w" + number + "a w" + number + "b w" + number + "c";
    }

    private static FeedbackRecord sent(int number, long at) {
        return new FeedbackRecord(at, FeedbackText.normalize(message(number)));
    }

    private static FeedbackCheck.Verdict verdict(String text, long now, List<FeedbackRecord> history) {
        return FeedbackLimiter.check(text, now, history).getVerdict();
    }

    @Test
    public void firstMessageIsAllowed() {
        assertEquals(FeedbackCheck.Verdict.ALLOWED, verdict("The scanner crops my page badly", NOW, new ArrayList<>()));
    }

    @Test
    public void tooFewCharactersOrWordsIsRejected() {
        List<FeedbackRecord> none = new ArrayList<>();
        assertEquals(FeedbackCheck.Verdict.TOO_SHORT, verdict("", NOW, none));
        assertEquals(FeedbackCheck.Verdict.TOO_SHORT, verdict("bug !!!", NOW, none));
        assertEquals(FeedbackCheck.Verdict.TOO_SHORT, verdict("aaaaaaaaaaaaaaaaaaaa", NOW, none));
        assertEquals(FeedbackCheck.Verdict.TOO_SHORT, verdict("?!?! ... ,,,, ----", NOW, none));
    }

    @Test
    public void textInAnyAlphabetCountsByItsWords() {
        assertEquals(FeedbackCheck.Verdict.ALLOWED, verdict("Сканер неправильно обрізає сторінку", NOW, new ArrayList<>()));
        assertEquals(FeedbackCheck.Verdict.ALLOWED, verdict("סורק חותך את העמוד לא נכון", NOW, new ArrayList<>()));
    }

    @Test
    public void sameTextWithOtherCaseAndPunctuationIsARepeat() {
        List<FeedbackRecord> history = new ArrayList<>();
        history.add(new FeedbackRecord(NOW - 5 * DAY, FeedbackText.normalize("The scanner crops my page badly")));
        assertEquals(FeedbackCheck.Verdict.DUPLICATE, verdict("the SCANNER crops, my page badly!!!", NOW, history));
    }

    @Test
    public void almostTheSameLongTextIsARepeatButAnotherThoughtIsNot() {
        String first = "one two three four five six seven eight nine ten";
        List<FeedbackRecord> history = new ArrayList<>();
        history.add(new FeedbackRecord(NOW - 5 * DAY, FeedbackText.normalize(first)));
        assertEquals(FeedbackCheck.Verdict.DUPLICATE,
                verdict("one two three four five six seven eight nine eleven", NOW, history));
        assertEquals(FeedbackCheck.Verdict.ALLOWED,
                verdict("one two three weather shows wrong city today", NOW, history));
    }

    @Test
    public void nextMessageMustWaitTwoMinutes() {
        List<FeedbackRecord> history = new ArrayList<>();
        history.add(sent(1, NOW - MINUTE));
        FeedbackCheck check = FeedbackLimiter.check(message(2), NOW, history);
        assertEquals(FeedbackCheck.Verdict.WAIT, check.getVerdict());
        assertEquals(MINUTE, check.getRetryAfterMillis());
        assertEquals(FeedbackCheck.Verdict.ALLOWED, verdict(message(2), NOW + MINUTE, history));
    }

    @Test
    public void fourthMessageInADayIsRejectedUntilTheOldestLeavesTheDay() {
        List<FeedbackRecord> history = new ArrayList<>();
        history.add(sent(1, NOW - 20 * HOUR));
        history.add(sent(2, NOW - 10 * HOUR));
        history.add(sent(3, NOW - 3 * HOUR));
        FeedbackCheck check = FeedbackLimiter.check(message(4), NOW, history);
        assertEquals(FeedbackCheck.Verdict.DAILY_LIMIT, check.getVerdict());
        assertEquals(4 * HOUR, check.getRetryAfterMillis());
        assertEquals(FeedbackCheck.Verdict.ALLOWED, verdict(message(4), NOW + 4 * HOUR, history));
    }

    @Test
    public void messagesOlderThanADayDoNotCountForTheDay() {
        List<FeedbackRecord> history = new ArrayList<>();
        history.add(sent(1, NOW - 26 * HOUR));
        history.add(sent(2, NOW - 25 * HOUR));
        history.add(sent(3, NOW - 24 * HOUR - MINUTE));
        assertEquals(FeedbackCheck.Verdict.ALLOWED, verdict(message(4), NOW, history));
    }

    @Test
    public void eleventhMessageInThirtyDaysIsRejectedEvenWhenTheDayIsFree() {
        List<FeedbackRecord> history = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            history.add(sent(i, NOW - (29 - 3 * i) * DAY - 12 * HOUR));
        }
        FeedbackCheck check = FeedbackLimiter.check(message(99), NOW, history);
        assertEquals(FeedbackCheck.Verdict.MONTHLY_LIMIT, check.getVerdict());
        assertEquals(12 * HOUR, check.getRetryAfterMillis());
    }

    @Test
    public void ordersOfTheHistoryDoNotMatter() {
        List<FeedbackRecord> history = new ArrayList<>();
        history.add(sent(3, NOW - 3 * HOUR));
        history.add(sent(1, NOW - 20 * HOUR));
        history.add(sent(2, NOW - 10 * HOUR));
        assertEquals(FeedbackCheck.Verdict.DAILY_LIMIT, verdict(message(4), NOW, history));
    }

    @Test
    public void aClockSetBackDoesNotOpenTheDoor() {
        List<FeedbackRecord> history = new ArrayList<>();
        history.add(sent(1, NOW + 5 * MINUTE));
        assertEquals(FeedbackCheck.Verdict.WAIT, verdict(message(2), NOW, history));
        assertTrue(FeedbackLimiter.check(message(2), NOW, history).getRetryAfterMillis() > 0);
    }
}
