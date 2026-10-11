package com.yurii.pavlenko.myassistant.scan.imaging;

/**
 * Removes grain from areas that are clearly blank paper by pulling their pixels to pure white.
 * An area counts as paper by its smoothed brightness, so pixels at ink edges and inside text keep their values.
 */
public final class PaperWhitener implements ImageFilter {

    private static final int SMOOTHING_RADIUS = 2;
    private static final float PAPER_START = 215f;
    private static final float PAPER_FULL = 240f;
    private static final float MIN_PIXEL = 170f;

    @Override
    public PixelImage apply(PixelImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = source.getPixels();
        float[] luminance = source.luminance();
        float[] smooth = BoxBlur.apply(luminance, width, height, SMOOTHING_RADIUS);

        int[] result = pixels.clone();
        for (int i = 0; i < pixels.length; i++) {
            if (luminance[i] < MIN_PIXEL || smooth[i] <= PAPER_START) {
                continue;
            }
            float paperness = Math.min(1f, (smooth[i] - PAPER_START) / (PAPER_FULL - PAPER_START));
            int argb = pixels[i];
            result[i] = Colors.rgb(
                    lift(Colors.red(argb), paperness),
                    lift(Colors.green(argb), paperness),
                    lift(Colors.blue(argb), paperness));
        }
        return new PixelImage(width, height, result);
    }

    private static int lift(int channel, float paperness) {
        return Math.round(channel + (255 - channel) * paperness);
    }
}
