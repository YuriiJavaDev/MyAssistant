package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/** Ice floes drifting slowly on the freezing sea. */
final class IceFloeLayer implements Layer {

    private static final int FLOES = 7;
    private static final int CORNERS = 8;

    private final Paint top = new Paint();
    private final Paint side = new Paint();
    private final Path shape = new Path();

    IceFloeLayer() {
        top.setColor(0xFFF1F9FF);
        side.setColor(0xFFA9CFE6);
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        for (int i = 0; i < FLOES; i++) {
            float x = Noise.fraction(Noise.unit(i, 24) + seconds * 0.008f * (0.7f + Noise.unit(i, 25))) * 1.3f - 0.15f;
            float y = SceneGeometry.HORIZON + 0.05f + (SceneGeometry.SHORE - SceneGeometry.HORIZON - 0.14f) * Noise.unit(i, 26)
                    + 0.004f * (float) Math.sin(seconds * 0.8f + i);
            float size = 0.035f + 0.04f * Noise.unit(i, 27);
            float depth = 0.012f + 0.015f * (y - SceneGeometry.HORIZON);
            drawFloe(canvas, x, y + depth, size, i, side);
            drawFloe(canvas, x, y, size, i, top);
        }
    }

    private void drawFloe(Canvas canvas, float x, float y, float size, int seed, Paint paint) {
        shape.reset();
        for (int c = 0; c < CORNERS; c++) {
            double angle = Math.PI * 2 * c / CORNERS;
            float radius = size * (0.7f + 0.5f * Noise.unit(seed * 10 + c, 28));
            float px = x + radius * (float) Math.cos(angle);
            float py = y + radius * 0.3f * (float) Math.sin(angle);
            if (c == 0) {
                shape.moveTo(px, py);
            } else {
                shape.lineTo(px, py);
            }
        }
        shape.close();
        canvas.drawPath(shape, paint);
    }
}
