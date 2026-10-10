package com.yurii.pavlenko.myassistant.scan.imaging;

/** Square maximum filter of a single float channel, used to bridge text strokes when estimating paper brightness. */
final class MaxFilter {

    private MaxFilter() {
    }

    static float[] apply(float[] channel, int width, int height, int radius) {
        float[] result = new float[channel.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float max = 0;
                for (int ny = Math.max(0, y - radius); ny <= Math.min(height - 1, y + radius); ny++) {
                    for (int nx = Math.max(0, x - radius); nx <= Math.min(width - 1, x + radius); nx++) {
                        max = Math.max(max, channel[ny * width + nx]);
                    }
                }
                result[y * width + x] = max;
            }
        }
        return result;
    }
}
