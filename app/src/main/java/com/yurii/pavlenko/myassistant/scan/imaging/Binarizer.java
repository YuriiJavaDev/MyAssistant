package com.yurii.pavlenko.myassistant.scan.imaging;

/** Pure black-and-white conversion with a threshold that follows the local brightness, so uneven light does not matter. */
public final class Binarizer implements ImageFilter {

    private static final int WINDOW_DIVISOR = 16;
    private static final int MIN_WINDOW_RADIUS = 7;
    private static final double INK_RATIO = 0.90;

    @Override
    public PixelImage apply(PixelImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        float[] luminance = source.luminance();
        long[] integral = integralImage(luminance, width, height);
        int radius = Math.max(MIN_WINDOW_RADIUS, Math.max(width, height) / WINDOW_DIVISOR / 2);

        int[] result = new int[luminance.length];
        for (int y = 0; y < height; y++) {
            int top = Math.max(0, y - radius);
            int bottom = Math.min(height - 1, y + radius);
            for (int x = 0; x < width; x++) {
                int left = Math.max(0, x - radius);
                int right = Math.min(width - 1, x + radius);
                long sum = integral[(bottom + 1) * (width + 1) + right + 1]
                        - integral[top * (width + 1) + right + 1]
                        - integral[(bottom + 1) * (width + 1) + left]
                        + integral[top * (width + 1) + left];
                double mean = (double) sum / ((right - left + 1) * (bottom - top + 1));
                boolean ink = luminance[y * width + x] < mean * INK_RATIO;
                result[y * width + x] = ink ? 0xFF000000 : 0xFFFFFFFF;
            }
        }
        return new PixelImage(width, height, result);
    }

    /** Summed-area table with one extra row and column of zeros, so any window sum takes four lookups. */
    private static long[] integralImage(float[] luminance, int width, int height) {
        long[] integral = new long[(width + 1) * (height + 1)];
        for (int y = 0; y < height; y++) {
            long rowSum = 0;
            for (int x = 0; x < width; x++) {
                rowSum += Math.round(luminance[y * width + x]);
                integral[(y + 1) * (width + 1) + x + 1] = integral[y * (width + 1) + x + 1] + rowSum;
            }
        }
        return integral;
    }
}
