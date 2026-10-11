package com.yurii.pavlenko.myassistant.scan.imaging;

/** Packed ARGB helpers shared by the imaging algorithms. */
final class Colors {

    private Colors() {
    }

    static int red(int argb) {
        return (argb >> 16) & 0xFF;
    }

    static int green(int argb) {
        return (argb >> 8) & 0xFF;
    }

    static int blue(int argb) {
        return argb & 0xFF;
    }

    static int rgb(int red, int green, int blue) {
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    static int clamp(int value) {
        return value < 0 ? 0 : Math.min(value, 255);
    }

    static float luminance(int argb) {
        return 0.299f * red(argb) + 0.587f * green(argb) + 0.114f * blue(argb);
    }
}
