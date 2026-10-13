package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * The near side of the moon as seen from the northern hemisphere: the main maria and well-known
 * craters at their real places, plus many small craters. Positions are in units of the moon's radius.
 */
final class MoonSurface {

    /** Maria as x, y and radius. */
    private static final float[][] MARIA = {
            {0.72f, -0.30f, 0.15f},   // Crisium
            {0.32f, -0.42f, 0.19f},   // Serenitatis
            {0.38f, -0.08f, 0.24f},   // Tranquillitatis
            {0.62f, 0.14f, 0.17f},    // Fecunditatis
            {0.40f, 0.30f, 0.10f},    // Nectaris
            {0.04f, -0.22f, 0.10f},   // Vaporum
            {-0.32f, -0.50f, 0.36f},  // Imbrium
            {-0.66f, -0.06f, 0.36f},  // Procellarum
            {-0.22f, 0.40f, 0.19f},   // Nubium
            {-0.60f, 0.40f, 0.12f},   // Humorum
    };
    /** Named craters as x, y, radius and brightness of the floor (1 bright, 0 dark). */
    private static final float[][] CRATERS = {
            {-0.12f, 0.76f, 0.050f, 1f},   // Tycho
            {-0.34f, -0.10f, 0.055f, 1f},  // Copernicus
            {-0.58f, -0.14f, 0.032f, 1f},  // Kepler
            {-0.56f, -0.42f, 0.036f, 1f},  // Aristarchus
            {-0.10f, -0.80f, 0.065f, 0f},  // Plato
            {-0.88f, 0.04f, 0.060f, 0f},   // Grimaldi
            {0.88f, 0.10f, 0.045f, 0.6f},  // Langrenus
            {0.28f, 0.24f, 0.050f, 0.6f},  // Theophilus
            {0.10f, 0.40f, 0.045f, 0.6f},  // Ptolemaeus
            {-0.22f, -0.60f, 0.030f, 0.8f},
    };
    private static final int RAY_COUNT = 9;
    private static final int SMALL_CRATERS = 70;
    private static final float[] TYCHO = {-0.12f, 0.76f};
    private static final float[] COPERNICUS = {-0.34f, -0.10f};

    private final Paint maria = new Paint();
    private final Paint rim = new Paint();
    private final Paint shadow = new Paint();
    private final Paint floor = new Paint();
    private final Paint ray = new Paint();
    private final Paint shading = new Paint();

    MoonSurface(float centerX, float centerY, float radius) {
        maria.setColor(0x3A706A58);
        rim.setColor(0xCCFFFFF0);
        shadow.setColor(0x55504A3C);
        floor.setColor(0x66605A48);
        ray.setColor(0x44FFFFF0);
        ray.setStyle(Paint.Style.STROKE);
        ray.setStrokeWidth(radius * 0.012f);
        shading.setShader(new RadialGradient(centerX, centerY, radius,
                new int[]{0x00000000, 0x00000000, 0x33403A2C}, new float[]{0f, 0.6f, 1f},
                Shader.TileMode.CLAMP));
    }

    void draw(Canvas canvas, float centerX, float centerY, float radius) {
        for (float[] sea : MARIA) {
            canvas.drawCircle(centerX + sea[0] * radius, centerY + sea[1] * radius, sea[2] * radius, maria);
        }
        drawRays(canvas, centerX, centerY, radius, TYCHO, 0.55f);
        drawRays(canvas, centerX, centerY, radius, COPERNICUS, 0.22f);
        for (int i = 0; i < SMALL_CRATERS; i++) {
            drawSmallCrater(canvas, centerX, centerY, radius, i);
        }
        for (float[] crater : CRATERS) {
            drawCrater(canvas, centerX + crater[0] * radius, centerY + crater[1] * radius,
                    crater[2] * radius, crater[3]);
        }
        canvas.drawCircle(centerX, centerY, radius, shading);
    }

    private void drawRays(Canvas canvas, float centerX, float centerY, float radius, float[] origin, float length) {
        float x = centerX + origin[0] * radius;
        float y = centerY + origin[1] * radius;
        for (int i = 0; i < RAY_COUNT; i++) {
            double angle = 2 * Math.PI * i / RAY_COUNT + Noise.unit(i, 7) * 0.5;
            float reach = radius * length * (0.6f + 0.4f * Noise.unit(i, 11));
            canvas.drawLine(x, y, x + reach * (float) Math.cos(angle), y + reach * (float) Math.sin(angle), ray);
        }
    }

    private void drawSmallCrater(Canvas canvas, float centerX, float centerY, float radius, int index) {
        double angle = 2 * Math.PI * Noise.unit(index, 21);
        float distance = 0.92f * (float) Math.sqrt(Noise.unit(index, 22));
        float size = radius * (0.012f + 0.022f * Noise.unit(index, 23));
        drawCrater(canvas, centerX + radius * distance * (float) Math.cos(angle),
                centerY + radius * distance * (float) Math.sin(angle), size, 0.5f);
    }

    /** A crater is a dark bowl lit on its far rim, which is what gives it depth. */
    private void drawCrater(Canvas canvas, float x, float y, float size, float brightness) {
        float lift = size * 0.25f;
        canvas.drawCircle(x, y, size, shadow);
        canvas.drawCircle(x - lift, y - lift, size * 0.88f, floor);
        rim.setAlpha((int) (60 + 150 * brightness));
        rim.setStyle(Paint.Style.STROKE);
        rim.setStrokeWidth(size * 0.22f);
        canvas.drawCircle(x, y, size, rim);
    }
}
