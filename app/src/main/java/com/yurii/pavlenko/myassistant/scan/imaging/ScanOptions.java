package com.yurii.pavlenko.myassistant.scan.imaging;

/** User-adjustable settings of the cleanup chain. Immutable; "with" methods return changed copies. */
public final class ScanOptions {

    private final ScanFilter filter;
    private final PaperFormat paperFormat;
    private final boolean removeShadows;
    private final boolean sharpen;
    private final int quarterTurns;

    public ScanOptions(ScanFilter filter, PaperFormat paperFormat, boolean removeShadows, boolean sharpen, int quarterTurns) {
        this.filter = filter;
        this.paperFormat = paperFormat;
        this.removeShadows = removeShadows;
        this.sharpen = sharpen;
        this.quarterTurns = ((quarterTurns % 4) + 4) % 4;
    }

    public static ScanOptions defaults() {
        return new ScanOptions(ScanFilter.COLOR, PaperFormat.AUTO, true, true, 0);
    }

    public ScanFilter getFilter() {
        return filter;
    }

    public PaperFormat getPaperFormat() {
        return paperFormat;
    }

    public boolean isRemoveShadows() {
        return removeShadows;
    }

    public boolean isSharpen() {
        return sharpen;
    }

    public int getQuarterTurns() {
        return quarterTurns;
    }

    public ScanOptions withFilter(ScanFilter newFilter) {
        return new ScanOptions(newFilter, paperFormat, removeShadows, sharpen, quarterTurns);
    }

    public ScanOptions withPaperFormat(PaperFormat newFormat) {
        return new ScanOptions(filter, newFormat, removeShadows, sharpen, quarterTurns);
    }

    public ScanOptions withRemoveShadows(boolean enabled) {
        return new ScanOptions(filter, paperFormat, enabled, sharpen, quarterTurns);
    }

    public ScanOptions withSharpen(boolean enabled) {
        return new ScanOptions(filter, paperFormat, removeShadows, enabled, quarterTurns);
    }

    public ScanOptions rotatedClockwise() {
        return new ScanOptions(filter, paperFormat, removeShadows, sharpen, quarterTurns + 1);
    }
}
