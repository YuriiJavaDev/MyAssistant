package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/** Two sun loungers under striped parasols, with people sunbathing when the weather allows. */
final class LoungerLayer implements Layer {

    private static final float[] CENTERS = {0.34f, 0.64f};
    private static final int[][] PARASOL = {{0xFFE53935, 0xFFFFFFFF}, {0xFF1E88E5, 0xFFFFD54F}};
    private static final int[] SWIMSUIT = {0xFFFF7043, 0xFF7E57C2};
    private static final int[] SKIN = {0xFFF1B98A, 0xFFC68642};
    private static final float BASE_Y = 0.84f;

    private final Paint fill = new Paint();
    private final Paint pole = new Paint();
    private final Path canopy = new Path();
    private final boolean occupied;

    LoungerLayer(boolean occupied) {
        this.occupied = occupied;
        pole.setColor(0xFF6D4C41);
        pole.setStyle(Paint.Style.STROKE);
        pole.setStrokeWidth(0.008f);
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        for (int i = 0; i < CENTERS.length; i++) {
            float x = CENTERS[i];
            drawParasol(canvas, x + 0.02f, i);
            drawLounger(canvas, x, i);
            if (occupied) {
                drawPerson(canvas, x, i, seconds);
            }
        }
    }

    private void drawParasol(Canvas canvas, float x, int index) {
        float top = BASE_Y - 0.17f;
        canvas.drawLine(x, top, x, BASE_Y + 0.02f, pole);
        canopy.reset();
        canopy.moveTo(x - 0.1f, top + 0.03f);
        canopy.quadTo(x, top - 0.09f, x + 0.1f, top + 0.03f);
        canopy.close();
        fill.setColor(PARASOL[index][0]);
        canvas.drawPath(canopy, fill);

        canopy.reset();
        canopy.moveTo(x - 0.035f, top + 0.03f);
        canopy.quadTo(x - 0.02f, top - 0.04f, x, top - 0.03f);
        canopy.quadTo(x + 0.02f, top - 0.04f, x + 0.035f, top + 0.03f);
        canopy.close();
        fill.setColor(PARASOL[index][1]);
        canvas.drawPath(canopy, fill);
    }

    private void drawLounger(Canvas canvas, float x, int index) {
        fill.setColor(0xFFF5F5F5);
        canvas.drawRect(x - 0.1f, BASE_Y - 0.012f, x + 0.1f, BASE_Y + 0.012f, fill);
        canvas.save();
        canvas.rotate(-35, x - 0.1f, BASE_Y);
        canvas.drawRect(x - 0.1f, BASE_Y - 0.01f, x - 0.1f + 0.07f, BASE_Y + 0.008f, fill);
        canvas.restore();
        fill.setColor(0xFF8D6E63);
        canvas.drawRect(x - 0.09f, BASE_Y + 0.012f, x - 0.08f, BASE_Y + 0.04f, fill);
        canvas.drawRect(x + 0.08f, BASE_Y + 0.012f, x + 0.09f, BASE_Y + 0.04f, fill);
    }

    private void drawPerson(Canvas canvas, float x, int index, float seconds) {
        float breath = 0.0015f * (float) Math.sin(seconds * 1.8 + index);
        fill.setColor(SKIN[index]);
        canvas.drawCircle(x - 0.075f, BASE_Y - 0.04f, 0.017f, fill);
        canvas.drawOval(x - 0.06f, BASE_Y - 0.038f - breath, x + 0.075f, BASE_Y - 0.012f + breath, fill);
        fill.setColor(SWIMSUIT[index]);
        canvas.drawOval(x - 0.015f, BASE_Y - 0.036f - breath, x + 0.035f, BASE_Y - 0.014f + breath, fill);
    }
}
