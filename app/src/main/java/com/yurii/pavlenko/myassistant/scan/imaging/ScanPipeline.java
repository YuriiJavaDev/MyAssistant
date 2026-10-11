package com.yurii.pavlenko.myassistant.scan.imaging;

import java.util.ArrayList;
import java.util.List;

/** Turns a photo and the page corners into a clean, upright, paper-shaped scan. */
public final class ScanPipeline {

    private ScanPipeline() {
    }

    /** Output long side is limited to maxLongSide, which lets previews run on a smaller budget than the final save. */
    public static ScanResult process(PixelImage photo, Quad corners, ScanOptions options, int maxLongSide) {
        double measuredWidth = (corners.edgeLength(0, photo.getWidth(), photo.getHeight())
                + corners.edgeLength(2, photo.getWidth(), photo.getHeight())) / 2;
        double measuredHeight = (corners.edgeLength(1, photo.getWidth(), photo.getHeight())
                + corners.edgeLength(3, photo.getWidth(), photo.getHeight())) / 2;

        PaperFormat format = options.getPaperFormat().resolve(measuredWidth, measuredHeight);
        int[] size = format.outputSize(measuredWidth, measuredHeight, maxLongSide);

        PixelImage image = PerspectiveWarp.warp(photo, corners, size[0], size[1]);
        for (ImageFilter filter : buildFilters(options)) {
            image = filter.apply(image);
        }
        return new ScanResult(image.rotatedClockwise(options.getQuarterTurns()), format);
    }

    private static List<ImageFilter> buildFilters(ScanOptions options) {
        List<ImageFilter> filters = new ArrayList<>();
        filters.add(new BorderCleaner());
        filters.add(new ChromaDenoiser());
        if (options.getShadowStrength() > 0) {
            filters.add(new ShadowRemover(options.getShadowStrength()));
        }
        filters.add(new ContrastStretch());
        filters.add(new PaperWhitener());

        if (options.getFilter() == ScanFilter.BLACK_WHITE) {
            addToneIfChanged(filters, options);
            filters.add(new GrayscaleFilter());
            filters.add(new Binarizer());
            return filters;
        }
        if (options.getFilter() == ScanFilter.GRAYSCALE) {
            filters.add(new GrayscaleFilter());
        }
        if (options.isSharpen()) {
            filters.add(new Sharpener());
        }
        addToneIfChanged(filters, options);
        return filters;
    }

    private static void addToneIfChanged(List<ImageFilter> filters, ScanOptions options) {
        if (options.getBrightness() != 0 || options.getContrast() != 0) {
            filters.add(new ToneAdjust(options.getBrightness(), options.getContrast()));
        }
    }
}
