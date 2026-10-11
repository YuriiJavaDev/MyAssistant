package com.yurii.pavlenko.myassistant.scan.imaging;

/** User-adjustable settings of the cleanup chain. Immutable; "with" methods return changed copies. */
public final class ScanOptions {

    private final ScanFilter filter;
    private final PaperFormat paperFormat;
    private final double shadowStrength;
    private final double brightness;
    private final double contrast;
    private final boolean sharpen;
    private final int quarterTurns;

    public ScanOptions(ScanFilter filter, PaperFormat paperFormat, double shadowStrength,
                       double brightness, double contrast, boolean sharpen, int quarterTurns) {
        this.filter = filter;
        this.paperFormat = paperFormat;
        this.shadowStrength = clampUnit(shadowStrength, 0, 1);
        this.brightness = clampUnit(brightness, -1, 1);
        this.contrast = clampUnit(contrast, -1, 1);
        this.sharpen = sharpen;
        this.quarterTurns = ((quarterTurns % 4) + 4) % 4;
    }

    public static ScanOptions defaults() {
        return new ScanOptions(ScanFilter.COLOR, PaperFormat.AUTO, 1, 0, 0, true, 0);
    }

    public ScanFilter getFilter() { return filter; }
    public PaperFormat getPaperFormat() { return paperFormat; }

    /** 0 keeps the lighting as photographed, 1 evens it out completely. */
    public double getShadowStrength() { return shadowStrength; }

    /** -1..1, shifts all tones; 0 leaves paper white and ink as dark as the levels step made them. */
    public double getBrightness() { return brightness; }

    /** -1..1, spreads (positive) or squeezes (negative) the tones around the middle grey. */
    public double getContrast() { return contrast; }

    public boolean isSharpen() { return sharpen; }
    public int getQuarterTurns() { return quarterTurns; }

    public ScanOptions withFilter(ScanFilter value) {
        return new ScanOptions(value, paperFormat, shadowStrength, brightness, contrast, sharpen, quarterTurns);
    }

    public ScanOptions withPaperFormat(PaperFormat value) {
        return new ScanOptions(filter, value, shadowStrength, brightness, contrast, sharpen, quarterTurns);
    }

    public ScanOptions withShadowStrength(double value) {
        return new ScanOptions(filter, paperFormat, value, brightness, contrast, sharpen, quarterTurns);
    }

    public ScanOptions withBrightness(double value) {
        return new ScanOptions(filter, paperFormat, shadowStrength, value, contrast, sharpen, quarterTurns);
    }

    public ScanOptions withContrast(double value) {
        return new ScanOptions(filter, paperFormat, shadowStrength, brightness, value, sharpen, quarterTurns);
    }

    public ScanOptions withSharpen(boolean value) {
        return new ScanOptions(filter, paperFormat, shadowStrength, brightness, contrast, value, quarterTurns);
    }

    public ScanOptions rotatedClockwise() {
        return new ScanOptions(filter, paperFormat, shadowStrength, brightness, contrast, sharpen, quarterTurns + 1);
    }

    private static double clampUnit(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
