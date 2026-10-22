package com.yurii.pavlenko.myassistant.flights.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.flights.logic.Cardinal;

import java.util.Locale;

/** A compass whose card turns under a fixed marker, as on an aircraft. */
public class CompassView extends InstrumentView {

    private static final int TICK_STEP = 10;
    private static final int LABEL_STEP = 30;
    private static final String[] POINTS = {"N", "E", "S", "W"};

    private final Paint tick = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint marker = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint plane = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint readout = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint box = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path shape = new Path();
    private final RectF boxBounds = new RectF();

    private double heading = Double.NaN;

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        tick.setColor(ink);
        tick.setStrokeCap(Paint.Cap.ROUND);
        label.setTextAlign(Paint.Align.CENTER);
        label.setFakeBoldText(true);
        marker.setColor(accent);
        plane.setColor(ink);
        readout.setTextAlign(Paint.Align.CENTER);
        readout.setFakeBoldText(true);
        readout.setColor(ink);
        box.setColor(ContextCompat.getColor(context, R.color.instrument_face));
    }

    /** @param degrees the heading from north, or NaN when it is not known */
    public void setHeading(double degrees) {
        heading = degrees;
        invalidate();
    }

    @Override
    void drawFace(Canvas canvas, float size) {
        float center = size / 2f;
        float radius = size / 2f;
        canvas.save();
        canvas.rotate(Double.isNaN(heading) ? 0f : (float) -heading, center, center);
        drawCard(canvas, center, radius);
        canvas.restore();
        drawMarker(canvas, center, radius);
        drawPlane(canvas, center, radius);
        drawReadout(canvas, center, radius);
    }

    private void drawCard(Canvas canvas, float center, float radius) {
        float outer = center - radius * 0.92f;
        for (int degrees = 0; degrees < 360; degrees += TICK_STEP) {
            boolean main = degrees % LABEL_STEP == 0;
            tick.setStrokeWidth(radius * (main ? 0.035f : 0.018f));
            canvas.save();
            canvas.rotate(degrees, center, center);
            canvas.drawLine(center, outer, center, outer + radius * (main ? 0.12f : 0.06f), tick);
            if (degrees % 90 == 0) {
                drawLabel(canvas, center, outer + radius * 0.34f, POINTS[degrees / 90], radius * 0.22f,
                        degrees == 0 ? accent : ink);
            } else if (main) {
                drawLabel(canvas, center, outer + radius * 0.30f, String.valueOf(degrees / TICK_STEP),
                        radius * 0.12f, dimInk);
            }
            canvas.restore();
        }
    }

    private void drawLabel(Canvas canvas, float x, float baseline, String text, float textSize, int color) {
        label.setTextSize(textSize);
        label.setColor(color);
        canvas.drawText(text, x, baseline, label);
    }

    private void drawMarker(Canvas canvas, float center, float radius) {
        float top = center - radius * 0.97f;
        shape.reset();
        shape.moveTo(center, top + radius * 0.1f);
        shape.lineTo(center - radius * 0.06f, top);
        shape.lineTo(center + radius * 0.06f, top);
        shape.close();
        canvas.drawPath(shape, marker);
    }

    private void drawPlane(Canvas canvas, float center, float radius) {
        float unit = radius * 0.01f;
        shape.reset();
        shape.moveTo(center, center - 24 * unit);
        shape.lineTo(center + 4 * unit, center - 10 * unit);
        shape.lineTo(center + 22 * unit, center + 4 * unit);
        shape.lineTo(center + 22 * unit, center + 9 * unit);
        shape.lineTo(center + 4 * unit, center + 3 * unit);
        shape.lineTo(center + 3 * unit, center + 16 * unit);
        shape.lineTo(center + 10 * unit, center + 22 * unit);
        shape.lineTo(center + 10 * unit, center + 26 * unit);
        shape.lineTo(center, center + 23 * unit);
        shape.lineTo(center - 10 * unit, center + 26 * unit);
        shape.lineTo(center - 10 * unit, center + 22 * unit);
        shape.lineTo(center - 3 * unit, center + 16 * unit);
        shape.lineTo(center - 4 * unit, center + 3 * unit);
        shape.lineTo(center - 22 * unit, center + 9 * unit);
        shape.lineTo(center - 22 * unit, center + 4 * unit);
        shape.lineTo(center - 4 * unit, center - 10 * unit);
        shape.close();
        canvas.drawPath(shape, plane);
    }

    private String cardinalName() {
        return getResources().getStringArray(R.array.flights_cardinal_points)[Cardinal.indexOf(heading)];
    }

    private void drawReadout(Canvas canvas, float center, float radius) {
        String text = Double.isNaN(heading) ? "--"
                : String.format(Locale.US, "%03.0f° %s", heading, cardinalName());
        readout.setTextSize(radius * 0.14f);
        float width = readout.measureText(text) + radius * 0.2f;
        float height = radius * 0.22f;
        boxBounds.set(center - width / 2f, center + radius * 0.32f, center + width / 2f, center + radius * 0.32f + height);
        canvas.drawRoundRect(boxBounds, height / 3f, height / 3f, box);
        canvas.drawText(text, center, boxBounds.bottom - height * 0.25f, readout);
    }
}
