package com.yurii.pavlenko.myassistant.scan.imaging;

/** User brightness and contrast, applied equally to all channels so hues stay put. */
public final class ToneAdjust implements ImageFilter {

    private static final double MAX_BRIGHTNESS_SHIFT = 80;

    private final int[] curve = new int[256];

    public ToneAdjust(double brightness, double contrast) {
        double gain = Math.pow(2, contrast);
        double shift = brightness * MAX_BRIGHTNESS_SHIFT;
        for (int level = 0; level < 256; level++) {
            curve[level] = Colors.clamp((int) Math.round((level - 128) * gain + 128 + shift));
        }
    }

    @Override
    public PixelImage apply(PixelImage source) {
        int[] pixels = source.getPixels();
        int[] result = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];
            result[i] = Colors.rgb(curve[Colors.red(argb)], curve[Colors.green(argb)], curve[Colors.blue(argb)]);
        }
        return new PixelImage(source.getWidth(), source.getHeight(), result);
    }
}
