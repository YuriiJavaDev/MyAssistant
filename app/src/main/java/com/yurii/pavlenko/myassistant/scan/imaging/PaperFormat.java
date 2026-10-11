package com.yurii.pavlenko.myassistant.scan.imaging;

/** Target page shape of a scan, including the PDF page size in points (1/72 inch). */
public enum PaperFormat {
    AUTO("Auto", 0, 0, 0),
    A4("A4", Math.sqrt(2), 595, 842),
    A5("A5", Math.sqrt(2), 420, 595),
    LETTER("Letter", 11.0 / 8.5, 612, 792),
    ORIGINAL("Original", 0, 0, 0);

    /** How far (relative) a measured page may differ from a standard ratio and still be snapped to it. */
    private static final double AUTO_SNAP_TOLERANCE = 0.10;
    private static final int MIN_LONG_SIDE = 600;

    private final String label;
    private final double longToShortRatio;
    private final int pdfShortSide;
    private final int pdfLongSide;

    PaperFormat(String label, double longToShortRatio, int pdfShortSide, int pdfLongSide) {
        this.label = label;
        this.longToShortRatio = longToShortRatio;
        this.pdfShortSide = pdfShortSide;
        this.pdfLongSide = pdfLongSide;
    }

    public String getLabel() {
        return label;
    }

    /** True for formats with a fixed physical page size. */
    public boolean isStandard() {
        return longToShortRatio > 0;
    }

    public int getPdfShortSide() {
        return pdfShortSide;
    }

    public int getPdfLongSide() {
        return pdfLongSide;
    }

    /**
     * Picks the concrete format for a page measured in pixels: explicit formats stay as they are,
     * AUTO snaps to A4 or Letter when the shape is close enough, otherwise the page stays ORIGINAL.
     */
    public PaperFormat resolve(double measuredWidth, double measuredHeight) {
        if (this != AUTO) {
            return this;
        }
        double measuredRatio = ratioOf(measuredWidth, measuredHeight);
        PaperFormat best = ORIGINAL;
        double bestDeviation = AUTO_SNAP_TOLERANCE;
        for (PaperFormat candidate : new PaperFormat[]{A4, LETTER}) {
            double deviation = Math.abs(measuredRatio - candidate.longToShortRatio) / candidate.longToShortRatio;
            if (deviation <= bestDeviation) {
                best = candidate;
                bestDeviation = deviation;
            }
        }
        return best;
    }

    /**
     * Output size in pixels (width, height) for a resolved format. The orientation follows the measured page,
     * the long side never exceeds maxLongSide and the shape is exactly the format's ratio when it has one.
     */
    public int[] outputSize(double measuredWidth, double measuredHeight, int maxLongSide) {
        double measuredLong = Math.max(measuredWidth, measuredHeight);
        double ratio = isStandard() ? longToShortRatio : ratioOf(measuredWidth, measuredHeight);
        int longSide = (int) Math.round(Math.max(MIN_LONG_SIDE, Math.min(measuredLong, maxLongSide)));
        int shortSide = Math.max(1, (int) Math.round(longSide / ratio));
        return measuredHeight >= measuredWidth
                ? new int[]{shortSide, longSide}
                : new int[]{longSide, shortSide};
    }

    /** PDF page size in points matching the scan's orientation, 595 points wide for formats without a physical size. */
    public int[] pdfPageSize(int imageWidth, int imageHeight) {
        boolean portrait = imageHeight >= imageWidth;
        if (isStandard()) {
            return portrait ? new int[]{pdfShortSide, pdfLongSide} : new int[]{pdfLongSide, pdfShortSide};
        }
        int fallbackWidth = A4.pdfShortSide;
        int fallbackHeight = (int) Math.round((double) fallbackWidth * imageHeight / imageWidth);
        return new int[]{fallbackWidth, Math.max(1, fallbackHeight)};
    }

    /** Safe lookup for stored names. */
    public static PaperFormat fromName(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException | NullPointerException e) {
            return ORIGINAL;
        }
    }

    private static double ratioOf(double width, double height) {
        return Math.max(width, height) / Math.max(1.0, Math.min(width, height));
    }
}
