package com.yurii.pavlenko.myassistant.flights.data;

import com.yurii.pavlenko.myassistant.flights.database.FlightDao;
import com.yurii.pavlenko.myassistant.flights.logic.ChunkedRead;

import java.io.IOException;

/**
 * Reads the stored PDF reports. A report is far bigger than the 2 MB a database cursor can hold, so it is read in
 * pieces.
 */
public final class FlightReportStore {

    private static final int CHUNK_BYTES = 512 * 1024;

    private final FlightDao dao;

    public FlightReportStore(FlightDao dao) {
        this.dao = dao;
    }

    /** The PDF of the flight; null when there is none. */
    public byte[] pdf(long flightId) throws IOException {
        Long size = dao.reportSize(flightId);
        if (size == null) {
            return null;
        }
        return ChunkedRead.all(size.intValue(), CHUNK_BYTES,
                (offset, length) -> dao.reportChunk(flightId, offset + 1, length));
    }
}
