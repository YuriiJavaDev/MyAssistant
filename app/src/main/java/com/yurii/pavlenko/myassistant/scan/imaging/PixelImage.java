package com.yurii.pavlenko.myassistant.scan.imaging;

/**
 * Plain ARGB pixel buffer. The whole imaging package works on it instead of android.graphics.Bitmap,
 * which keeps the algorithms free of Android dependencies and testable on a plain JVM.
 */
public final class PixelImage {

    private final int width;
    private final int height;
    private final int[] pixels;

    public PixelImage(int width, int height, int[] pixels) {
        if (width <= 0 || height <= 0 || pixels.length != width * height) {
            throw new IllegalArgumentException("Pixel buffer does not match " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public static PixelImage blank(int width, int height, int argb) {
        int[] pixels = new int[width * height];
        java.util.Arrays.fill(pixels, argb);
        return new PixelImage(width, height, pixels);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getPixels() {
        return pixels;
    }

    public int get(int x, int y) {
        return pixels[y * width + x];
    }

    public void set(int x, int y, int argb) {
        pixels[y * width + x] = argb;
    }

    /** Luminance of every pixel in the 0..255 range. */
    public float[] luminance() {
        float[] result = new float[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            result[i] = Colors.luminance(pixels[i]);
        }
        return result;
    }

    /** Box-averaged copy whose longer side is at most maxSide; returns this when it is already small enough. */
    public PixelImage downscaledTo(int maxSide) {
        int longSide = Math.max(width, height);
        if (longSide <= maxSide) {
            return this;
        }
        double scale = (double) maxSide / longSide;
        int newWidth = Math.max(1, (int) Math.round(width * scale));
        int newHeight = Math.max(1, (int) Math.round(height * scale));
        int[] result = new int[newWidth * newHeight];

        for (int y = 0; y < newHeight; y++) {
            int y0 = y * height / newHeight;
            int y1 = Math.max(y0 + 1, (y + 1) * height / newHeight);
            for (int x = 0; x < newWidth; x++) {
                int x0 = x * width / newWidth;
                int x1 = Math.max(x0 + 1, (x + 1) * width / newWidth);
                result[y * newWidth + x] = averageBlock(x0, x1, y0, y1);
            }
        }
        return new PixelImage(newWidth, newHeight, result);
    }

    /** Copy turned clockwise by the given number of quarter turns (any integer, negative turns go counter-clockwise). */
    public PixelImage rotatedClockwise(int quarterTurns) {
        int turns = ((quarterTurns % 4) + 4) % 4;
        if (turns == 0) {
            return this;
        }
        boolean swapsSides = turns % 2 == 1;
        int newWidth = swapsSides ? height : width;
        int newHeight = swapsSides ? width : height;
        int[] result = new int[pixels.length];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int nx;
                int ny;
                switch (turns) {
                    case 1:
                        nx = height - 1 - y;
                        ny = x;
                        break;
                    case 2:
                        nx = width - 1 - x;
                        ny = height - 1 - y;
                        break;
                    default:
                        nx = y;
                        ny = width - 1 - x;
                        break;
                }
                result[ny * newWidth + nx] = pixels[y * width + x];
            }
        }
        return new PixelImage(newWidth, newHeight, result);
    }

    private int averageBlock(int x0, int x1, int y0, int y1) {
        long red = 0;
        long green = 0;
        long blue = 0;
        int count = (x1 - x0) * (y1 - y0);
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                int argb = pixels[y * width + x];
                red += Colors.red(argb);
                green += Colors.green(argb);
                blue += Colors.blue(argb);
            }
        }
        return Colors.rgb((int) (red / count), (int) (green / count), (int) (blue / count));
    }
}
