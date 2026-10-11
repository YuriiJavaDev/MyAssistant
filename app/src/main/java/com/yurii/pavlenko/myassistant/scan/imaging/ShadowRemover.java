package com.yurii.pavlenko.myassistant.scan.imaging;

/**
 * Evens out uneven lighting and shadows. The paper brightness is estimated on a coarse grid
 * and every pixel is scaled by one gain for all three channels, so colours keep their hue.
 */
public final class ShadowRemover implements ImageFilter {

    private static final int GRID_CELLS_ON_LONG_SIDE = 120;
    private static final double BRIGHT_PERCENTILE = 0.85;
    private static final int HISTOGRAM_BINS = 64;
    private static final int TEXT_BRIDGE_CELLS = 5;
    private static final float MAX_GAIN = 4f;
    private static final float MIN_BACKGROUND = 32f;

    @Override
    public PixelImage apply(PixelImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int cell = Math.max(4, Math.max(width, height) / GRID_CELLS_ON_LONG_SIDE);
        int gridWidth = (width + cell - 1) / cell;
        int gridHeight = (height + cell - 1) / cell;

        float[] background = estimateBackground(source.luminance(), width, height, cell, gridWidth, gridHeight);
        background = MaxFilter.apply(background, gridWidth, gridHeight, TEXT_BRIDGE_CELLS);
        background = BoxBlur.apply(background, gridWidth, gridHeight, TEXT_BRIDGE_CELLS);

        int[] result = new int[source.getPixels().length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float paper = Math.max(MIN_BACKGROUND, sampleGrid(background, gridWidth, gridHeight, (x + 0.5f) / cell - 0.5f, (y + 0.5f) / cell - 0.5f));
                float gain = Math.min(MAX_GAIN, 255f / paper);
                int argb = source.getPixels()[y * width + x];
                result[y * width + x] = Colors.rgb(
                        Colors.clamp(Math.round(Colors.red(argb) * gain)),
                        Colors.clamp(Math.round(Colors.green(argb) * gain)),
                        Colors.clamp(Math.round(Colors.blue(argb) * gain)));
            }
        }
        return new PixelImage(width, height, result);
    }

    private static float[] estimateBackground(float[] luminance, int width, int height, int cell, int gridWidth, int gridHeight) {
        float[] grid = new float[gridWidth * gridHeight];
        int[] histogram = new int[HISTOGRAM_BINS];
        for (int gy = 0; gy < gridHeight; gy++) {
            for (int gx = 0; gx < gridWidth; gx++) {
                java.util.Arrays.fill(histogram, 0);
                int count = 0;
                for (int y = gy * cell; y < Math.min(height, (gy + 1) * cell); y++) {
                    for (int x = gx * cell; x < Math.min(width, (gx + 1) * cell); x++) {
                        histogram[Math.min(HISTOGRAM_BINS - 1, (int) (luminance[y * width + x] * HISTOGRAM_BINS / 256f))]++;
                        count++;
                    }
                }
                grid[gy * gridWidth + gx] = percentile(histogram, count);
            }
        }
        return grid;
    }

    private static float percentile(int[] histogram, int count) {
        int target = (int) Math.ceil(count * BRIGHT_PERCENTILE);
        int seen = 0;
        for (int bin = 0; bin < histogram.length; bin++) {
            seen += histogram[bin];
            if (seen >= target) {
                return (bin + 1) * 256f / HISTOGRAM_BINS;
            }
        }
        return 255f;
    }

    private static float sampleGrid(float[] grid, int gridWidth, int gridHeight, float x, float y) {
        float cx = Math.max(0, Math.min(x, gridWidth - 1));
        float cy = Math.max(0, Math.min(y, gridHeight - 1));
        int x0 = (int) cx;
        int y0 = (int) cy;
        int x1 = Math.min(x0 + 1, gridWidth - 1);
        int y1 = Math.min(y0 + 1, gridHeight - 1);
        float fx = cx - x0;
        float fy = cy - y0;
        float top = grid[y0 * gridWidth + x0] * (1 - fx) + grid[y0 * gridWidth + x1] * fx;
        float bottom = grid[y1 * gridWidth + x0] * (1 - fx) + grid[y1 * gridWidth + x1] * fx;
        return top * (1 - fy) + bottom * fy;
    }
}
