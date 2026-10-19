package com.yurii.pavlenko.myassistant.tasks.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ConvertersTest {

    @Test
    public void dateIsStoredAsDaysSinceTheEpoch() {
        assertEquals(Long.valueOf(0), Converters.dateToTimestamp(LocalDate.of(1970, 1, 1)));
        assertEquals(Long.valueOf(1), Converters.dateToTimestamp(LocalDate.of(1970, 1, 2)));
        assertEquals(Long.valueOf(-1), Converters.dateToTimestamp(LocalDate.of(1969, 12, 31)));
        assertEquals(LocalDate.of(2000, 1, 1), Converters.fromTimestampToDate(10957L));
    }

    @Test
    public void dateTimeIsStoredAsUtcSeconds() {
        assertEquals(Long.valueOf(60), Converters.dateTimeToTimestamp(LocalDateTime.of(1970, 1, 1, 0, 1, 0)));
        assertEquals(LocalDateTime.of(2001, 9, 9, 1, 46, 40), Converters.fromTimestampToDateTime(1_000_000_000L));
    }

    @Test
    public void fractionsOfASecondAreDroppedOnTheWayIn() {
        LocalDateTime withNanos = LocalDateTime.of(2026, 10, 18, 12, 30, 15, 987_000_000);

        LocalDateTime restored = Converters.fromTimestampToDateTime(Converters.dateTimeToTimestamp(withNanos));

        assertEquals(LocalDateTime.of(2026, 10, 18, 12, 30, 15), restored);
    }

    @Test
    public void missingValuesStayMissing() {
        assertNull(Converters.dateToTimestamp(null));
        assertNull(Converters.fromTimestampToDate(null));
        assertNull(Converters.dateTimeToTimestamp(null));
        assertNull(Converters.fromTimestampToDateTime(null));
    }
}
