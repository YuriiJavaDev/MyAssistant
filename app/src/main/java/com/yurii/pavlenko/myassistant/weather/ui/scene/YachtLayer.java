package com.yurii.pavlenko.myassistant.weather.ui.scene;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/** A single-masted yacht with white sails that tacks back and forth far out at sea. */
final class YachtLayer implements Layer {

    private static final float LEG_SECONDS = 38f;
    private static final float MIN_X = 0.24f;
    private static final float MAX_X = 0.76f;
    private static final float WATERLINE = SceneGeometry.HORIZON + 0.055f;
    private static final float HEEL_DEGREES = 5f;

    private final Paint hull = new Paint();
    private final Paint sail = new Paint();
    private final Paint shade = new Paint();
    private final Paint mast = new Paint();
    private final Paint flag = new Paint();
    private final Paint wake = new Paint();
    private final Path shape = new Path();

    YachtLayer() {
        hull.setColor(0xFF1E3A5F);
        sail.setColor(0xFFFFFFFF);
        shade.setColor(0xFFDDE7F0);
        mast.setColor(0xFF5D4037);
        mast.setStyle(Paint.Style.STROKE);
        mast.setStrokeWidth(0.003f);
        flag.setColor(0xFFE53935);
        wake.setColor(0xAAFFFFFF);
        wake.setStyle(Paint.Style.STROKE);
        wake.setStrokeWidth(0.003f);
        wake.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    public void draw(Canvas canvas, float seconds) {
        float leg = seconds / LEG_SECONDS;
        float phase = Noise.fraction(leg / 2);
        float along = 1 - Math.abs(2 * phase - 1);
        float x = MIN_X + (MAX_X - MIN_X) * along;
        int facing = phase < 0.5f ? 1 : -1;
        float y = WATERLINE + 0.003f * (float) Math.sin(seconds * 1.3f);

        canvas.save();
        canvas.scale(facing, 1, x, y);
        canvas.rotate(HEEL_DEGREES + 1.5f * (float) Math.sin(seconds * 0.9f), x, y);
        drawWake(canvas, x, y, seconds);
        drawSails(canvas, x, y);
        drawHull(canvas, x, y);
        canvas.restore();
    }

    private void drawWake(Canvas canvas, float x, float y, float seconds) {
        float shimmer = 0.004f * (float) Math.sin(seconds * 3f);
        canvas.drawLine(x - 0.05f, y + 0.003f, x - 0.075f - shimmer, y + 0.004f, wake);
        canvas.drawLine(x - 0.045f, y + 0.007f, x - 0.065f + shimmer, y + 0.008f, wake);
    }

    private void drawSails(Canvas canvas, float x, float y) {
        float top = y - 0.085f;
        canvas.drawLine(x, y, x, top, mast);

        shape.reset();
        shape.moveTo(x - 0.004f, top + 0.004f);
        shape.lineTo(x - 0.004f, y - 0.01f);
        shape.lineTo(x - 0.046f, y - 0.01f);
        shape.close();
        canvas.drawPath(shape, sail);

        shape.reset();
        shape.moveTo(x + 0.004f, top + 0.008f);
        shape.lineTo(x + 0.004f, y - 0.01f);
        shape.lineTo(x + 0.04f, y - 0.01f);
        shape.close();
        canvas.drawPath(shape, shade);

        shape.reset();
        shape.moveTo(x, top);
        shape.lineTo(x + 0.014f, top + 0.004f);
        shape.lineTo(x, top + 0.008f);
        shape.close();
        canvas.drawPath(shape, flag);
    }

    private void drawHull(Canvas canvas, float x, float y) {
        shape.reset();
        shape.moveTo(x - 0.05f, y - 0.008f);
        shape.lineTo(x + 0.052f, y - 0.008f);
        shape.quadTo(x + 0.04f, y + 0.008f, x + 0.02f, y + 0.009f);
        shape.lineTo(x - 0.036f, y + 0.009f);
        shape.quadTo(x - 0.05f, y + 0.004f, x - 0.05f, y - 0.008f);
        shape.close();
        canvas.drawPath(shape, hull);
    }
}
