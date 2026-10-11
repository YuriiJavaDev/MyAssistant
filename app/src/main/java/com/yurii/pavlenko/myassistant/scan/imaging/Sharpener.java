package com.yurii.pavlenko.myassistant.scan.imaging;

/**
 * Unsharp mask on luminance only: edges get crisper while colour noise is not amplified,
 * and a single float buffer is enough, which matters for full-size photos.
 */
public final class Sharpener implements ImageFilter {

    private static final float AMOUNT = 0.6f;
    private static final int PIXELS_PER_RADIUS_STEP = 1200;

    @Override
    public PixelImage apply(PixelImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int radius = Math.max(1, Math.max(width, height) / PIXELS_PER_RADIUS_STEP);
        int[] pixels = source.getPixels();

        float[] luminance = source.luminance();
        float[] blurred = BoxBlur.apply(luminance, width, height, radius);

        int[] result = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            int boost = Math.round(AMOUNT * (luminance[i] - blurred[i]));
            int argb = pixels[i];
            result[i] = Colors.rgb(
                    Colors.clamp(Colors.red(argb) + boost),
                    Colors.clamp(Colors.green(argb) + boost),
                    Colors.clamp(Colors.blue(argb) + boost));
        }
        return new PixelImage(width, height, result);
    }
}
