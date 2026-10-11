package com.yurii.pavlenko.myassistant.scan.imaging;

/** Replaces colours with their luminance. */
public final class GrayscaleFilter implements ImageFilter {

    @Override
    public PixelImage apply(PixelImage source) {
        int[] pixels = source.getPixels();
        int[] result = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            int gray = Math.round(Colors.luminance(pixels[i]));
            result[i] = Colors.rgb(gray, gray, gray);
        }
        return new PixelImage(source.getWidth(), source.getHeight(), result);
    }
}
