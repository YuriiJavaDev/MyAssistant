package com.yurii.pavlenko.myassistant.flights.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.yurii.pavlenko.myassistant.R;

/** The artificial horizon: sky and ground that tilt and slide against a fixed little aircraft. */
public class HorizonView extends InstrumentView {

    private static final float VISIBLE_DEGREES = 40f;
    private static final int PITCH_STEP = 10;
    private static final int[] BANK_MARKS = {-60, -45, -30, -20, -10, 0, 10, 20, 30, 45, 60};

    private final Paint sky = new Paint();
    private final Paint ground = new Paint();
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint plane = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path pointer = new Path();

    private double pitch;
    private double roll;

    public HorizonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        sky.setColor(ContextCompat.getColor(context, R.color.instrument_sky));
        ground.setColor(ContextCompat.getColor(context, R.color.instrument_ground));
        line.setColor(ink);
        line.setStrokeCap(Paint.Cap.ROUND);
        plane.setColor(accent);
        plane.setStrokeCap(Paint.Cap.ROUND);
    }

    /** @param pitchDegrees nose above the horizon; @param rollDegrees lean to the right. NaN counts as level. */
    public void setAttitude(double pitchDegrees, double rollDegrees) {
        pitch = Double.isNaN(pitchDegrees) ? 0 : pitchDegrees;
        roll = Double.isNaN(rollDegrees) ? 0 : rollDegrees;
        invalidate();
    }

    @Override
    void drawFace(Canvas canvas, float size) {
        float center = size / 2f;
        float radius = size / 2f;
        float perDegree = radius / VISIBLE_DEGREES;

        canvas.save();
        canvas.rotate((float) -roll, center, center);
        canvas.save();
        canvas.translate(0, (float) pitch * perDegree);
        canvas.drawRect(-size, -size, 2 * size, center, sky);
        canvas.drawRect(-size, center, 2 * size, 3 * size, ground);
        drawLadder(canvas, center, radius, perDegree);
        canvas.restore();
        drawBankScale(canvas, center, radius);
        canvas.restore();

        drawPointer(canvas, center, radius);
        drawPlane(canvas, center, radius);
    }

    private void drawLadder(Canvas canvas, float center, float radius, float perDegree) {
        line.setStrokeWidth(radius * 0.025f);
        canvas.drawLine(center - radius * 2, center, center + radius * 2, center, line);
        line.setStrokeWidth(radius * 0.015f);
        for (int degrees = PITCH_STEP; degrees <= 3 * PITCH_STEP; degrees += PITCH_STEP) {
            float half = radius * (degrees % (2 * PITCH_STEP) == 0 ? 0.3f : 0.18f);
            float offset = degrees * perDegree;
            canvas.drawLine(center - half, center - offset, center + half, center - offset, line);
            canvas.drawLine(center - half, center + offset, center + half, center + offset, line);
        }
    }

    private void drawBankScale(Canvas canvas, float center, float radius) {
        float outer = center - radius * 0.9f;
        for (int mark : BANK_MARKS) {
            boolean major = mark % 30 == 0;
            line.setStrokeWidth(radius * (major ? 0.03f : 0.018f));
            canvas.save();
            canvas.rotate(mark, center, center);
            canvas.drawLine(center, outer, center, outer + radius * (major ? 0.1f : 0.06f), line);
            canvas.restore();
        }
    }

    private void drawPointer(Canvas canvas, float center, float radius) {
        float top = center - radius * 0.78f;
        pointer.reset();
        pointer.moveTo(center, top);
        pointer.lineTo(center - radius * 0.06f, top + radius * 0.1f);
        pointer.lineTo(center + radius * 0.06f, top + radius * 0.1f);
        pointer.close();
        canvas.drawPath(pointer, plane);
    }

    private void drawPlane(Canvas canvas, float center, float radius) {
        plane.setStrokeWidth(radius * 0.06f);
        canvas.drawLine(center - radius * 0.5f, center, center - radius * 0.16f, center, plane);
        canvas.drawLine(center + radius * 0.16f, center, center + radius * 0.5f, center, plane);
        canvas.drawLine(center - radius * 0.16f, center, center - radius * 0.16f, center + radius * 0.08f, plane);
        canvas.drawLine(center + radius * 0.16f, center, center + radius * 0.16f, center + radius * 0.08f, plane);
        canvas.drawCircle(center, center, radius * 0.05f, plane);
    }
}
