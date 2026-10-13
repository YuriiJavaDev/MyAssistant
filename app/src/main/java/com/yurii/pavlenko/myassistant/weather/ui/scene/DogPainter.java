package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;

/** Draws a small trotting dog. */
final class DogPainter {

    private static final int COAT = 0xFF8D6E63;
    private static final int EAR = 0xFF5D4037;

    private final Paint fill = new Paint();
    private final Paint limb = new Paint();

    DogPainter() {
        limb.setStyle(Paint.Style.STROKE);
        limb.setStrokeCap(Paint.Cap.ROUND);
        limb.setColor(COAT);
    }

    /** @param facing 1 for a dog running to the right, -1 to the left */
    void draw(Canvas canvas, float x, float baseY, float size, float stride, int facing) {
        canvas.save();
        canvas.scale(facing, 1, x, baseY);
        float trot = 0.2f * size * (float) Math.sin(stride);
        float bodyY = baseY - 0.55f * size;

        limb.setStrokeWidth(0.16f * size);
        canvas.drawLine(x - 0.45f * size, bodyY + 0.1f * size, x - 0.45f * size + trot, baseY, limb);
        canvas.drawLine(x - 0.25f * size, bodyY + 0.1f * size, x - 0.25f * size - trot, baseY, limb);
        canvas.drawLine(x + 0.3f * size, bodyY + 0.1f * size, x + 0.3f * size - trot, baseY, limb);
        canvas.drawLine(x + 0.5f * size, bodyY + 0.1f * size, x + 0.5f * size + trot, baseY, limb);

        fill.setColor(COAT);
        canvas.drawOval(x - 0.6f * size, bodyY - 0.28f * size, x + 0.6f * size, bodyY + 0.28f * size, fill);
        canvas.drawCircle(x + 0.65f * size, bodyY - 0.32f * size, 0.27f * size, fill);
        fill.setColor(EAR);
        canvas.drawOval(x + 0.55f * size, bodyY - 0.62f * size, x + 0.72f * size, bodyY - 0.28f * size, fill);

        limb.setStrokeWidth(0.1f * size);
        float wag = 0.15f * size * (float) Math.sin(stride * 2.5f);
        canvas.drawLine(x - 0.58f * size, bodyY - 0.1f * size, x - 0.85f * size, bodyY - 0.45f * size + wag, limb);
        canvas.restore();
    }
}
