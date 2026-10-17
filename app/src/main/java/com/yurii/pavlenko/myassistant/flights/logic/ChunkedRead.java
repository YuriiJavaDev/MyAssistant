package com.yurii.pavlenko.myassistant.flights.logic;

import java.io.IOException;

/** Reads a large value piece by piece, for sources that cannot hand over more than a limited size at once. */
public final class ChunkedRead {

    /** Gives up to length bytes that start at the offset (counted from zero). */
    public interface Source {
        byte[] chunk(long offset, int length) throws IOException;
    }

    private ChunkedRead() {
    }

    /**
     * @param size the whole size in bytes, as the source knows it
     * @throws IOException when the source gives less than it should
     */
    public static byte[] all(int size, int chunkSize, Source source) throws IOException {
        byte[] whole = new byte[size];
        int done = 0;
        while (done < size) {
            int length = Math.min(chunkSize, size - done);
            byte[] chunk = source.chunk(done, length);
            if (chunk == null || chunk.length != length) {
                throw new IOException("The data is cut short at byte " + done);
            }
            System.arraycopy(chunk, 0, whole, done, length);
            done += length;
        }
        return whole;
    }
}
