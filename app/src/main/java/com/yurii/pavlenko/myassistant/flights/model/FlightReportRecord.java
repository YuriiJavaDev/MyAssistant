package com.yurii.pavlenko.myassistant.flights.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

/** The PDF report of a flight, kept apart from the totals so that the journal list never loads it. */
@Entity(tableName = "flight_reports",
        foreignKeys = @ForeignKey(entity = FlightRecord.class, parentColumns = "id", childColumns = "flightId",
                onDelete = ForeignKey.CASCADE))
public class FlightReportRecord {

    @PrimaryKey
    private long flightId;
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] pdf;
    /** False when the map is only the offline coastline or an older, blurrier one, so the report is made again once a connection is there. */
    @ColumnInfo(defaultValue = "0")
    private boolean onlineMap;

    public FlightReportRecord(long flightId, byte[] pdf, boolean onlineMap) {
        this.flightId = flightId;
        this.pdf = pdf;
        this.onlineMap = onlineMap;
    }

    public long getFlightId() { return flightId; }
    public byte[] getPdf() { return pdf; }
    public boolean isOnlineMap() { return onlineMap; }
}
