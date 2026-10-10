package com.yurii.pavlenko.myassistant.scan.imaging;

/** A finished scan together with the concrete paper format it was shaped to. */
public final class ScanResult {

    private final PixelImage image;
    private final PaperFormat paperFormat;

    public ScanResult(PixelImage image, PaperFormat paperFormat) {
        this.image = image;
        this.paperFormat = paperFormat;
    }

    public PixelImage getImage() {
        return image;
    }

    public PaperFormat getPaperFormat() {
        return paperFormat;
    }
}
