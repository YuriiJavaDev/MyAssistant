package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;

/** Clouds drifting across the sky from left to right. */
final class CloudLayer implements Layer {

    private final Paint paint = new Paint();
    private final int count;
    private final float speed;
    private final float minY;
    private final float maxY;
    private final float size;

    CloudLayer(Palette palette, int count, float speed, float minY, float maxY, float size) {
        this.count = count;
        this.speed = speed;
        this.minY = minY;
        this.maxY = maxY;
        this.size = size;
        paint.setColor(palette.cloud);
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        for (int i = 0; i < count; i++) {
            float scale = size * (0.8f + 0.4f * Noise.unit(i, 5));
            float position = i / (float) count + 0.2f * Noise.unit(i, 6) + seconds * speed * (0.8f + 0.4f * Noise.unit(i, 7));
            float x = Noise.fraction(position) * 1.5f - 0.3f;
            float y = minY + (maxY - minY) * Noise.unit(i, 8);
            drawCloud(canvas, x, y, scale);
        }
    }

    private void drawCloud(Canvas canvas, float x, float y, float scale) {
        canvas.drawCircle(x, y, 0.06f * scale, paint);
        canvas.drawCircle(x + 0.08f * scale, y - 0.03f * scale, 0.075f * scale, paint);
        canvas.drawCircle(x + 0.17f * scale, y, 0.06f * scale, paint);
        canvas.drawRect(x, y - 0.0f, x + 0.17f * scale, y + 0.06f * scale, paint);
        canvas.drawCircle(x, y + 0.03f * scale, 0.03f * scale, paint);
        canvas.drawCircle(x + 0.17f * scale, y + 0.03f * scale, 0.03f * scale, paint);
    }
}
