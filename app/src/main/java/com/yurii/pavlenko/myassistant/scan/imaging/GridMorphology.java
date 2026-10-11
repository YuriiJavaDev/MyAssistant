package com.yurii.pavlenko.myassistant.scan.imaging;

/** Square-window dilation (maximum) and erosion (minimum) of a single float channel. */
final class GridMorphology {

    private GridMorphology() {
    }

    static float[] dilate(float[] channel, int width, int height, int radius) {
        return apply(channel, width, height, radius, true);
    }

    static float[] erode(float[] channel, int width, int height, int radius) {
        return apply(channel, width, height, radius, false);
    }

    /** Separable: a horizontal pass followed by a vertical one gives the same result as the full square window. */
    private static float[] apply(float[] channel, int width, int height, int radius, boolean takeMax) {
        float[] horizontal = new float[channel.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float best = channel[y * width + x];
                for (int nx = Math.max(0, x - radius); nx <= Math.min(width - 1, x + radius); nx++) {
                    best = pick(best, channel[y * width + nx], takeMax);
                }
                horizontal[y * width + x] = best;
            }
        }
        float[] result = new float[channel.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float best = horizontal[y * width + x];
                for (int ny = Math.max(0, y - radius); ny <= Math.min(height - 1, y + radius); ny++) {
                    best = pick(best, horizontal[ny * width + x], takeMax);
                }
                result[y * width + x] = best;
            }
        }
        return result;
    }

    private static float pick(float current, float candidate, boolean takeMax) {
        return takeMax ? Math.max(current, candidate) : Math.min(current, candidate);
    }
}
