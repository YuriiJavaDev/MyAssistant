package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;

/** Twinkling stars scattered over the upper part of the sky. */
final class StarsLayer implements Layer {

    private final Paint paint = new Paint();
    private final int count;
    private final float maxY;

    StarsLayer(int count, float maxY) {
        this.count = count;
        this.maxY = maxY;
        paint.setColor(0xFFFFFFFF);
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        for (int i = 0; i < count; i++) {
            float twinkle = 0.45f + 0.55f * (float) Math.sin(seconds * (1.2f + Noise.unit(i, 3)) + i);
            paint.setAlpha((int) (255 * Math.max(0.1f, twinkle)));
            canvas.drawCircle(Noise.unit(i, 1), Noise.unit(i, 2) * maxY, 0.004f + 0.004f * Noise.unit(i, 4), paint);
        }
    }
}
