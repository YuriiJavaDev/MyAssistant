package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/** Flashes of lightning every few seconds, each along a different bolt. */
final class LightningLayer implements Layer {

    private static final float PERIOD = 5.3f;
    private static final float FLASH = 0.14f;
    private static final float SECOND_FLASH_AT = 0.24f;
    private static final int SEGMENTS = 6;

    private final Paint flash = new Paint();
    private final Paint bolt = new Paint();
    private final Path path = new Path();

    LightningLayer() {
        flash.setColor(0xFFFFFFFF);
        bolt.setColor(0xFFFFF9C4);
        bolt.setStyle(Paint.Style.STROKE);
        bolt.setStrokeWidth(0.012f);
        bolt.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        int strike = (int) (seconds / PERIOD);
        float phase = seconds - strike * PERIOD;
        boolean first = phase < FLASH;
        boolean second = phase > SECOND_FLASH_AT && phase < SECOND_FLASH_AT + FLASH * 0.6f;
        if (!first && !second) {
            return;
        }
        flash.setAlpha(first ? 110 : 60);
        canvas.drawRect(0, 0, 1, 1, flash);
        if (first) {
            drawBolt(canvas, strike);
        }
    }

    private void drawBolt(Canvas canvas, int strike) {
        float x = 0.25f + 0.5f * Noise.unit(strike, 22);
        float y = 0.22f;
        path.reset();
        path.moveTo(x, y);
        for (int i = 0; i < SEGMENTS; i++) {
            x += (Noise.unit(strike * 10 + i, 23) - 0.5f) * 0.12f;
            y += (SceneGeometry.SHORE - 0.22f) / SEGMENTS;
            path.lineTo(x, y);
        }
        canvas.drawPath(path, bolt);
    }
}
