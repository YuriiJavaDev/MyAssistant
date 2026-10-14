package com.yurii.pavlenko.myassistant.flights.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.yurii.pavlenko.myassistant.flights.logic.NiceScale;

import java.util.Locale;

/** A round gauge with a needle that sweeps over three quarters of a circle, and a digital readout. */
public class DialView extends InstrumentView {

    private static final float START_ANGLE = 135f;
    private static final float SWEEP = 270f;
    private static final int MAJOR_TICKS = 5;
    private static final int MINOR_PER_MAJOR = 5;
    private static final double THOUSANDS_FROM = 10_000;

    private final Paint tick = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint needle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint readout = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint caption = new Paint(Paint.ANTI_ALIAS_FLAG);

    private String title = "";
    private String unit = "";
    private double min;
    private double max = 10;
    private boolean autoRange;
    private int decimals;
    private double value;

    public DialView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        tick.setStrokeCap(Paint.Cap.ROUND);
        label.setTextAlign(Paint.Align.CENTER);
        readout.setTextAlign(Paint.Align.CENTER);
        readout.setFakeBoldText(true);
        caption.setTextAlign(Paint.Align.CENTER);
        needle.setColor(accent);
        needle.setStrokeCap(Paint.Cap.ROUND);
        tick.setColor(ink);
        label.setColor(ink);
        readout.setColor(ink);
        caption.setColor(dimInk);
    }

    /** A dial with a fixed range. */
    public void configure(String title, double min, double max, int decimals) {
        this.title = title;
        this.min = min;
        this.max = max;
        this.autoRange = false;
        this.decimals = decimals;
        invalidate();
    }

    /** A dial from 0 whose range grows to the next round number when the value outgrows it. */
    public void configureAutoRange(String title, int decimals) {
        configure(title, 0, NiceScale.maxFor(0), decimals);
        autoRange = true;
    }

    public void setUnit(String unit) {
        this.unit = unit;
        invalidate();
    }

    /** Back to the smallest range, as before a flight. */
    public void resetRange() {
        if (autoRange) {
            max = NiceScale.maxFor(0);
            invalidate();
        }
    }

    public void setValue(double newValue) {
        value = newValue;
        if (autoRange) {
            max = Math.max(max, NiceScale.maxFor(Math.abs(newValue)));
        }
        invalidate();
    }

    @Override
    void drawFace(Canvas canvas, float size) {
        float center = size / 2f;
        float radius = size / 2f;
        drawScale(canvas, center, radius);
        drawNeedle(canvas, center, radius);
        drawReadout(canvas, center, radius);
    }

    private void drawScale(Canvas canvas, float center, float radius) {
        int steps = MAJOR_TICKS * MINOR_PER_MAJOR;
        float outer = radius * 0.92f;
        label.setTextSize(radius * 0.13f);
        for (int i = 0; i <= steps; i++) {
            boolean major = i % MINOR_PER_MAJOR == 0;
            float angle = START_ANGLE + SWEEP * i / steps;
            tick.setStrokeWidth(radius * (major ? 0.035f : 0.018f));
            canvas.save();
            canvas.rotate(angle, center, center);
            canvas.drawLine(center + outer - radius * (major ? 0.12f : 0.06f), center, center + outer, center, tick);
            canvas.restore();
            if (major) {
                double shown = min + (max - min) * i / steps;
                double radians = Math.toRadians(angle);
                float labelRadius = radius * 0.64f;
                canvas.drawText(scaleLabel(shown),
                        center + (float) Math.cos(radians) * labelRadius,
                        center + (float) Math.sin(radians) * labelRadius + label.getTextSize() / 3f, label);
            }
        }
    }

    private void drawNeedle(Canvas canvas, float center, float radius) {
        double fraction = Math.max(0, Math.min(1, (value - min) / (max - min)));
        needle.setStrokeWidth(radius * 0.04f);
        canvas.save();
        canvas.rotate(START_ANGLE + SWEEP * (float) fraction, center, center);
        canvas.drawLine(center - radius * 0.1f, center, center + radius * 0.78f, center, needle);
        canvas.restore();
        canvas.drawCircle(center, center, radius * 0.06f, needle);
    }

    private void drawReadout(Canvas canvas, float center, float radius) {
        caption.setTextSize(radius * 0.12f);
        canvas.drawText(title, center, center - radius * 0.3f, caption);
        readout.setTextSize(radius * 0.2f);
        canvas.drawText(format(value, decimals), center, center + radius * 0.62f, readout);
        canvas.drawText(unit, center, center + radius * 0.8f, caption);
    }

    /** Ranges of thousands are labelled 4k, 8k, 12k so that the labels stay short. */
    private String scaleLabel(double shown) {
        return max >= THOUSANDS_FROM && shown != 0 ? format(shown / 1000, 0) + "k" : format(shown, 0);
    }

    private static String format(double number, int decimals) {
        return String.format(Locale.US, "%." + decimals + "f", number);
    }
}
