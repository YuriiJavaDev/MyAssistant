package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;

import java.util.List;

/** The animated landscape shown in the porthole. */
public final class Scene {

    private final List<Layer> layers;

    Scene(List<Layer> layers) {
        this.layers = layers;
    }

    /** Draws into a square of the given size at the canvas origin. */
    public void draw(Canvas canvas, float size, float seconds) {
        canvas.save();
        canvas.scale(size, size);
        for (Layer layer : layers) {
            layer.draw(canvas, seconds);
        }
        canvas.restore();
    }
}
