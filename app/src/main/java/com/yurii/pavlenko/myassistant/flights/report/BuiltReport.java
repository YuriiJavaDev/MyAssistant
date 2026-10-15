package com.yurii.pavlenko.myassistant.flights.report;

/** A finished report and whether its map is the full online one, as opposed to the offline coastline. */
public final class BuiltReport {

    private final byte[] pdf;
    private final boolean onlineMap;

    BuiltReport(byte[] pdf, boolean onlineMap) {
        this.pdf = pdf;
        this.onlineMap = onlineMap;
    }

    public byte[] getPdf() { return pdf; }
    public boolean hasOnlineMap() { return onlineMap; }
}
