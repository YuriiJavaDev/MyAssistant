package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;

/** Butterflies that wander over the beach, beating their wings. */
final class ButterflyLayer implements Layer {

    private static final int[] COLORS = {0xFFFF9800, 0xFFFFEB3B, 0xFF64B5F6, 0xFFF48FB1};
    private static final float WING_SPEED = 13f;

    private final Paint wing = new Paint();
    private final Paint body = new Paint();

    ButterflyLayer() {
        body.setColor(0xFF4E342E);
        body.setStyle(Paint.Style.STROKE);
        body.setStrokeWidth(0.006f);
        body.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        for (int i = 0; i < COLORS.length; i++) {
            float x = 0.5f + 0.27f * (float) Math.sin(seconds * (0.35f + 0.08f * i) + i * 1.9f);
            float y = 0.62f + 0.12f * (float) Math.sin(seconds * (0.55f + 0.05f * i) + i * 2.7f) - 0.04f * i;
            float spread = Math.abs((float) Math.sin(seconds * WING_SPEED + i));
            drawButterfly(canvas, x, y, spread, COLORS[i]);
        }
    }

    private void drawButterfly(Canvas canvas, float x, float y, float spread, int color) {
        wing.setColor(color);
        float width = 0.012f + 0.016f * spread;
        canvas.drawOval(x - width - 0.003f, y - 0.018f, x - 0.003f, y + 0.004f, wing);
        canvas.drawOval(x + 0.003f, y - 0.018f, x + width + 0.003f, y + 0.004f, wing);
        canvas.drawOval(x - width * 0.8f, y, x - 0.003f, y + 0.016f, wing);
        canvas.drawOval(x + 0.003f, y, x + width * 0.8f, y + 0.016f, wing);
        canvas.drawLine(x, y - 0.014f, x, y + 0.014f, body);
    }
}
