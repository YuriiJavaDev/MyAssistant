package com.yurii.pavlenko.myassistant.scan.imaging;

/** Unsharp mask: adds back the difference between each pixel and its blurred surroundings. */
public final class Sharpener implements ImageFilter {

    private static final float AMOUNT = 0.8f;
    private static final int PIXELS_PER_RADIUS_STEP = 1200;

    @Override
    public PixelImage apply(PixelImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int radius = Math.max(1, Math.max(width, height) / PIXELS_PER_RADIUS_STEP);
        int[] pixels = source.getPixels();

        float[] red = sharpenChannel(pixels, width, height, radius, 16);
        float[] green = sharpenChannel(pixels, width, height, radius, 8);
        float[] blue = sharpenChannel(pixels, width, height, radius, 0);

        int[] result = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            result[i] = Colors.rgb(Colors.clamp(Math.round(red[i])), Colors.clamp(Math.round(green[i])), Colors.clamp(Math.round(blue[i])));
        }
        return new PixelImage(width, height, result);
    }

    private static float[] sharpenChannel(int[] pixels, int width, int height, int radius, int shift) {
        float[] channel = new float[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            channel[i] = (pixels[i] >> shift) & 0xFF;
        }
        float[] blurred = BoxBlur.apply(channel, width, height, radius);
        for (int i = 0; i < channel.length; i++) {
            channel[i] += AMOUNT * (channel[i] - blurred[i]);
        }
        return channel;
    }
}
