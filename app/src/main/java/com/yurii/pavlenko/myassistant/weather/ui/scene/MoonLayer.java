package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;

/** A crescent moon in the night sky above the beach. */
final class MoonLayer implements Layer {

    private static final float X = 0.72f;
    private static final float Y = 0.22f;

    private final Paint moon = new Paint();
    private final Paint bite = new Paint();

    MoonLayer(Palette palette) {
        moon.setColor(0xFFFFF6D6);
        bite.setColor(Palette.blend(palette.skyTop, palette.skyBottom, 0.25f));
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        canvas.drawCircle(X, Y, 0.07f, moon);
        canvas.drawCircle(X + 0.03f, Y - 0.015f, 0.063f, bite);
    }
}
