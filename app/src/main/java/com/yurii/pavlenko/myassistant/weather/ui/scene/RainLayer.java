package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;

/** Falling rain; the stronger it is, the more drops and the steeper they fall. */
final class RainLayer implements Layer {

    private final Paint paint = new Paint();
    private final int drops;
    private final float slant;
    private final float length;
    private final float speed;

    RainLayer(int drops, float slant, float length, float speed) {
        this.drops = drops;
        this.slant = slant;
        this.length = length;
        this.speed = speed;
        paint.setColor(0xAAC9E4F5);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0.004f);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        for (int i = 0; i < drops; i++) {
            float fall = speed * (0.85f + 0.3f * Noise.unit(i, 15));
            float y = Noise.fraction(Noise.unit(i, 16) + seconds * fall) * 1.2f - 0.1f;
            float x = Noise.unit(i, 17) * 1.2f - 0.1f + slant * y;
            canvas.drawLine(x, y, x + slant * length, y + length, paint);
        }
    }
}
