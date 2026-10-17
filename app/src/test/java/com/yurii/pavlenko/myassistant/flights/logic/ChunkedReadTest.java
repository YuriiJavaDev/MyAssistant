package com.yurii.pavlenko.myassistant.flights.logic;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ChunkedReadTest {

    private static byte[] numbers(int count) {
        byte[] data = new byte[count];
        for (int i = 0; i < count; i++) {
            data[i] = (byte) (i * 7 + 3);
        }
        return data;
    }

    @Test
    public void joinsThePiecesIntoTheWholeValue() throws IOException {
        byte[] data = numbers(1000);
        byte[] whole = ChunkedRead.all(1000, 300, (offset, length) -> Arrays.copyOfRange(data, (int) offset, (int) offset + length));
        assertArrayEquals(data, whole);
    }

    @Test
    public void asksForTheLastPieceOnlyAsLongAsItIs() throws IOException {
        byte[] data = numbers(1000);
        List<Integer> asked = new ArrayList<>();
        ChunkedRead.all(1000, 300, (offset, length) -> {
            asked.add(length);
            return Arrays.copyOfRange(data, (int) offset, (int) offset + length);
        });
        assertEquals(Arrays.asList(300, 300, 300, 100), asked);
    }

    @Test
    public void readsAnEmptyValueWithoutAskingTheSource() throws IOException {
        byte[] whole = ChunkedRead.all(0, 300, (offset, length) -> {
            throw new IOException("must not be asked");
        });
        assertEquals(0, whole.length);
    }

    @Test
    public void failsWhenThePieceIsShort() {
        try {
            ChunkedRead.all(100, 50, (offset, length) -> new byte[length - 1]);
            fail("a short piece must be reported");
        } catch (IOException expected) {
            assertEquals("The data is cut short at byte 0", expected.getMessage());
        }
    }

    @Test
    public void failsWhenThePieceIsMissing() {
        try {
            ChunkedRead.all(100, 50, (offset, length) -> offset == 0 ? new byte[length] : null);
            fail("a missing piece must be reported");
        } catch (IOException expected) {
            assertEquals("The data is cut short at byte 50", expected.getMessage());
        }
    }
}
