package com.yurii.pavlenko.myassistant.scan.imaging;

/** Stretches the tonal range so the paper becomes white and the ink dark, using one factor for all channels to keep hues. */
public final class ContrastStretch implements ImageFilter {

    private static final double BLACK_PERCENTILE = 0.01;
    private static final double PAPER_PERCENTILE = 0.90;
    private static final float MIN_RANGE = 32f;

    @Override
    public PixelImage apply(PixelImage source) {
        int[] pixels = source.getPixels();
        int[] histogram = new int[256];
        for (int argb : pixels) {
            histogram[Math.min(255, (int) Colors.luminance(argb))]++;
        }

        float black = levelAtPercentile(histogram, pixels.length, BLACK_PERCENTILE);
        float white = Math.max(black + MIN_RANGE, levelAtPercentile(histogram, pixels.length, PAPER_PERCENTILE));
        float scale = 255f / (white - black);

        int[] result = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];
            result[i] = Colors.rgb(
                    stretch(Colors.red(argb), black, scale),
                    stretch(Colors.green(argb), black, scale),
                    stretch(Colors.blue(argb), black, scale));
        }
        return new PixelImage(source.getWidth(), source.getHeight(), result);
    }

    private static int stretch(int channel, float black, float scale) {
        return Colors.clamp(Math.round((channel - black) * scale));
    }

    private static float levelAtPercentile(int[] histogram, int total, double share) {
        long target = (long) Math.ceil(total * share);
        long seen = 0;
        for (int level = 0; level < histogram.length; level++) {
            seen += histogram[level];
            if (seen >= target) {
                return level;
            }
        }
        return 255f;
    }
}
