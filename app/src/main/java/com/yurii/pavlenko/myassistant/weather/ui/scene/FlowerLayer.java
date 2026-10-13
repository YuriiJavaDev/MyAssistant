package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/** Grass and colourful flowers along the bottom edge of the porthole, swaying in the breeze. */
final class FlowerLayer implements Layer {

    private static final int BLADES = 46;
    private static final int FLOWERS = 9;
    private static final float MIN_X = 0.2f;
    private static final float MAX_X = 0.8f;
    private static final int[] PETALS = {0xFFFF5252, 0xFFFFEB3B, 0xFFE040FB, 0xFFFF9800, 0xFFFFFFFF, 0xFF40C4FF};
    private static final int PETAL_COUNT = 5;

    private final Paint grass = new Paint();
    private final Paint stem = new Paint();
    private final Paint petal = new Paint();
    private final Paint heart = new Paint();
    private final Path blade = new Path();
    private final boolean flowers;

    FlowerLayer(Palette palette, boolean flowers) {
        this.flowers = flowers;
        grass.setColor(palette.grass);
        stem.setColor(palette.grass);
        stem.setStyle(Paint.Style.STROKE);
        stem.setStrokeWidth(0.006f);
        heart.setColor(0xFFFFC107);
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        for (int i = 0; i < BLADES; i++) {
            float x = MIN_X + (MAX_X - MIN_X) * i / (BLADES - 1) + 0.01f * Noise.unit(i, 11);
            float height = 0.05f + 0.06f * Noise.unit(i, 12);
            float lean = 0.012f * (float) Math.sin(seconds * 1.5f + i * 0.7f);
            drawBlade(canvas, x, SceneGeometry.rimBottom(x) + 0.01f, height, lean);
        }
        if (!flowers) {
            return;
        }
        for (int i = 0; i < FLOWERS; i++) {
            float x = MIN_X + 0.03f + (MAX_X - MIN_X - 0.06f) * Noise.unit(i, 13);
            float height = 0.09f + 0.06f * Noise.unit(i, 14);
            float lean = 0.014f * (float) Math.sin(seconds * 1.3f + i * 1.1f);
            drawFlower(canvas, x, SceneGeometry.rimBottom(x), height, lean, PETALS[i % PETALS.length]);
        }
    }

    private void drawBlade(Canvas canvas, float x, float baseY, float height, float lean) {
        blade.reset();
        blade.moveTo(x - 0.008f, baseY);
        blade.quadTo(x + lean * 0.4f, baseY - height * 0.6f, x + lean, baseY - height);
        blade.quadTo(x + lean * 0.4f + 0.004f, baseY - height * 0.5f, x + 0.008f, baseY);
        blade.close();
        canvas.drawPath(blade, grass);
    }

    private void drawFlower(Canvas canvas, float x, float baseY, float height, float lean, int color) {
        float headX = x + lean;
        float headY = baseY - height;
        blade.reset();
        blade.moveTo(x, baseY);
        blade.quadTo(x + lean * 0.3f, baseY - height * 0.5f, headX, headY);
        canvas.drawPath(blade, stem);

        petal.setColor(color);
        float reach = 0.017f;
        for (int p = 0; p < PETAL_COUNT; p++) {
            double angle = Math.PI * 2 * p / PETAL_COUNT;
            canvas.drawCircle(headX + reach * (float) Math.cos(angle), headY + reach * (float) Math.sin(angle), 0.014f, petal);
        }
        canvas.drawCircle(headX, headY, 0.011f, heart);
    }
}
