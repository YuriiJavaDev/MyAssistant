package com.yurii.pavlenko.myassistant.scan.imaging;

/**
 * Repairs the outermost lines of a warped page when the chosen corners sat a little outside the paper and
 * let the desk show through. Lines at the border that are clearly darker than the paper just inside them
 * are replaced by the first good line.
 */
public final class BorderCleaner implements ImageFilter {

    private static final double MAX_DEPTH_FRACTION = 0.03;
    private static final double REFERENCE_START_FRACTION = 0.03;
    private static final double REFERENCE_END_FRACTION = 0.05;
    private static final double DARK_LINE_RATIO = 0.88;

    @Override
    public PixelImage apply(PixelImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        float[] luma = source.luminance();
        int[] pixels = source.getPixels().clone();

        int top = badDepth(rowMeans(luma, width, height));
        int bottom = badDepth(reversed(rowMeans(luma, width, height)));
        int left = badDepth(columnMeans(luma, width, height));
        int right = badDepth(reversed(columnMeans(luma, width, height)));

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < top; y++) {
                pixels[y * width + x] = pixels[top * width + x];
            }
            for (int y = 0; y < bottom; y++) {
                pixels[(height - 1 - y) * width + x] = pixels[(height - 1 - bottom) * width + x];
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < left; x++) {
                pixels[y * width + x] = pixels[y * width + left];
            }
            for (int x = 0; x < right; x++) {
                pixels[y * width + width - 1 - x] = pixels[y * width + width - 1 - right];
            }
        }
        return new PixelImage(width, height, pixels);
    }

    /** Number of lines from the start that are too dark; the lines are ordered from the border inward. */
    private static int badDepth(float[] means) {
        int length = means.length;
        int maxDepth = (int) (length * MAX_DEPTH_FRACTION);
        float reference = 0;
        for (int i = (int) (length * REFERENCE_START_FRACTION); i <= (int) (length * REFERENCE_END_FRACTION) && i < length; i++) {
            reference = Math.max(reference, means[i]);
        }
        int depth = 0;
        while (depth < maxDepth && means[depth] < reference * DARK_LINE_RATIO) {
            depth++;
        }
        return depth;
    }

    private static float[] rowMeans(float[] luma, int width, int height) {
        float[] means = new float[height];
        for (int y = 0; y < height; y++) {
            double sum = 0;
            for (int x = 0; x < width; x++) {
                sum += luma[y * width + x];
            }
            means[y] = (float) (sum / width);
        }
        return means;
    }

    private static float[] columnMeans(float[] luma, int width, int height) {
        float[] means = new float[width];
        for (int x = 0; x < width; x++) {
            double sum = 0;
            for (int y = 0; y < height; y++) {
                sum += luma[y * width + x];
            }
            means[x] = (float) (sum / height);
        }
        return means;
    }

    private static float[] reversed(float[] values) {
        float[] result = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[values.length - 1 - i];
        }
        return result;
    }
}
