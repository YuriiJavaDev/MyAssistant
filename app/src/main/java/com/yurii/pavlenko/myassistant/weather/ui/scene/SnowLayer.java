package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;

/** Snowflakes that sink slowly and sway from side to side. */
final class SnowLayer implements Layer {

    private static final int FLAKES = 70;
    private static final float FALL_SPEED = 0.09f;

    private final Paint paint = new Paint();

    SnowLayer() {
        paint.setColor(0xFFFFFFFF);
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        for (int i = 0; i < FLAKES; i++) {
            float fall = FALL_SPEED * (0.6f + 0.8f * Noise.unit(i, 18));
            float y = Noise.fraction(Noise.unit(i, 19) + seconds * fall) * 1.1f - 0.05f;
            float x = Noise.unit(i, 20) + 0.03f * (float) Math.sin(seconds * 0.8f + i);
            canvas.drawCircle(x, y, 0.005f + 0.007f * Noise.unit(i, 21), paint);
        }
    }
}
