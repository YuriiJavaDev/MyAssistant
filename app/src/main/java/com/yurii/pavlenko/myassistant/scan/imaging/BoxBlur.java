package com.yurii.pavlenko.myassistant.scan.imaging;

/** Separable box blur of a single float channel; edges are clamped. */
final class BoxBlur {

    private BoxBlur() {
    }

    static float[] apply(float[] channel, int width, int height, int radius) {
        if (radius <= 0) {
            return channel.clone();
        }
        float[] horizontal = new float[channel.length];
        for (int y = 0; y < height; y++) {
            blurLine(channel, horizontal, y * width, 1, width, radius);
        }
        float[] result = new float[channel.length];
        for (int x = 0; x < width; x++) {
            blurLine(horizontal, result, x, width, height, radius);
        }
        return result;
    }

    /** Running-sum blur of one row or column: offset is the first index, stride the step between samples. */
    private static void blurLine(float[] input, float[] output, int offset, int stride, int length, int radius) {
        double window = 2.0 * radius + 1;
        double sum = 0;
        for (int i = -radius; i <= radius; i++) {
            sum += input[offset + clampIndex(i, length) * stride];
        }
        for (int i = 0; i < length; i++) {
            output[offset + i * stride] = (float) (sum / window);
            sum += input[offset + clampIndex(i + radius + 1, length) * stride]
                    - input[offset + clampIndex(i - radius, length) * stride];
        }
    }

    private static int clampIndex(int index, int length) {
        return index < 0 ? 0 : Math.min(index, length - 1);
    }
}
