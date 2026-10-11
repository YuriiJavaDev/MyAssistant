package com.yurii.pavlenko.myassistant.scan.imaging;

/** One step of the cleanup chain; implementations return a new image and never modify the input. */
public interface ImageFilter {

    PixelImage apply(PixelImage source);
}
