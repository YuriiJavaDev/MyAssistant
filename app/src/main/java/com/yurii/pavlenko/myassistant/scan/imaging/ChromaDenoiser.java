package com.yurii.pavlenko.myassistant.scan.imaging;

/**
 * Smooths the colour of the image while keeping its brightness untouched, so coloured blocks such as stamps
 * lose sensor colour noise and text stays sharp. The blur radius grows with the image size, because the noise
 * grain does too.
 */
public final class ChromaDenoiser implements ImageFilter {

    private static final int PIXELS_PER_RADIUS_STEP = 500;
    private static final int MAX_RADIUS = 6;

    @Override
    public PixelImage apply(PixelImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int radius = Math.max(1, Math.min(MAX_RADIUS, Math.max(width, height) / PIXELS_PER_RADIUS_STEP));

        int[] pixels = source.getPixels();
        float[] luma = source.luminance();
        float[] red = new float[pixels.length];
        float[] blue = new float[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            red[i] = Colors.red(pixels[i]) - luma[i];
            blue[i] = Colors.blue(pixels[i]) - luma[i];
        }
        red = BoxBlur.apply(red, width, height, radius);
        blue = BoxBlur.apply(blue, width, height, radius);

        int[] result = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            result[i] = Colors.fromLumaAndChroma(luma[i], red[i], blue[i]);
        }
        return new PixelImage(width, height, result);
    }
}
