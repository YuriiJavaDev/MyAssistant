package com.yurii.pavlenko.myassistant.flights.report;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.yurii.pavlenko.myassistant.flights.logic.FlightTime;
import com.yurii.pavlenko.myassistant.flights.logic.NiceNumbers;

import java.util.List;
import java.util.Locale;

/** Draws a value against the time of the flight as a line over a light area, with a grid and labelled axes. */
final class ProfileChartPainter {

    private static final int MAX_POINTS = 400;
    private static final int Y_TICKS = 4;
    private static final int X_TICKS = 4;
    private static final float TEXT_SIZE = 7.5f;
    private static final float TITLE_SIZE = 9f;
    private static final float LEFT_MARGIN = 34f;
    private static final float TOP_MARGIN = 16f;
    private static final float BOTTOM_MARGIN = 14f;
    private static final int FILL_ALPHA = 40;

    private final Paint frame = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint grid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint title = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();

    ProfileChartPainter() {
        frame.setColor(ReportColors.LINE);
        frame.setStyle(Paint.Style.STROKE);
        frame.setStrokeWidth(0.8f);
        grid.setColor(0xFFE0E0E0);
        grid.setStyle(Paint.Style.STROKE);
        grid.setStrokeWidth(0.5f);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(1.4f);
        line.setStrokeJoin(Paint.Join.ROUND);
        fill.setStyle(Paint.Style.FILL);
        text.setColor(ReportColors.MUTED);
        text.setTextSize(TEXT_SIZE);
        title.setColor(ReportColors.TEXT);
        title.setTextSize(TITLE_SIZE);
        title.setFakeBoldText(true);
    }

    /**
     * @param seconds the time of every value from the start of the flight, rising
     * @param values  the values in the unit of the chart
     */
    void draw(Canvas canvas, RectF area, String heading, List<Double> seconds, List<Double> values, int color) {
        canvas.drawRect(area, frame);
        canvas.drawText(heading, area.left + 6, area.top + 11, title);
        if (values.size() < 2) {
            return;
        }
        RectF plot = new RectF(area.left + LEFT_MARGIN, area.top + TOP_MARGIN, area.right - 8, area.bottom - BOTTOM_MARGIN);
        double duration = Math.max(1, seconds.get(seconds.size() - 1));
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (double value : values) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        List<Double> yTicks = NiceNumbers.ticks(min, max, Y_TICKS);
        double low = Math.min(min, yTicks.get(0));
        double high = Math.max(max, yTicks.get(yTicks.size() - 1));
        if (high - low < 1e-9) {
            high = low + 1;
        }
        drawYGrid(canvas, plot, yTicks, low, high);
        drawXGrid(canvas, plot, duration);
        drawSeries(canvas, plot, seconds, values, duration, low, high, color);
    }

    private void drawYGrid(Canvas canvas, RectF plot, List<Double> ticks, double low, double high) {
        text.setTextAlign(Paint.Align.RIGHT);
        for (double tick : ticks) {
            float y = (float) (plot.bottom - (tick - low) / (high - low) * plot.height());
            canvas.drawLine(plot.left, y, plot.right, y, grid);
            canvas.drawText(label(tick), plot.left - 3, y + 2.5f, text);
        }
        text.setTextAlign(Paint.Align.LEFT);
    }

    private void drawXGrid(Canvas canvas, RectF plot, double duration) {
        for (int i = 0; i <= X_TICKS; i++) {
            float x = plot.left + plot.width() * i / X_TICKS;
            canvas.drawLine(x, plot.top, x, plot.bottom, grid);
            text.setTextAlign(i == 0 ? Paint.Align.LEFT : i == X_TICKS ? Paint.Align.RIGHT : Paint.Align.CENTER);
            canvas.drawText(FlightTime.format(Math.round(duration * i / X_TICKS * 1000)), x, plot.bottom + 10, text);
        }
        text.setTextAlign(Paint.Align.LEFT);
    }

    private void drawSeries(Canvas canvas, RectF plot, List<Double> seconds, List<Double> values,
                            double duration, double low, double high, int color) {
        int stride = Math.max(1, (int) Math.ceil(values.size() / (double) MAX_POINTS));
        linePath.reset();
        fillPath.reset();
        float x = plot.left;
        for (int i = 0; i < values.size(); i += stride) {
            int index = Math.min(i, values.size() - 1);
            x = (float) (plot.left + seconds.get(index) / duration * plot.width());
            float y = (float) (plot.bottom - (values.get(index) - low) / (high - low) * plot.height());
            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, plot.bottom);
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }
        fillPath.lineTo(x, plot.bottom);
        fillPath.close();
        fill.setColor(color);
        fill.setAlpha(FILL_ALPHA);
        canvas.drawPath(fillPath, fill);
        line.setColor(color);
        canvas.drawPath(linePath, line);
        canvas.drawRect(plot, frame);
    }

    private static String label(double value) {
        return Math.abs(value) >= 10 || value == Math.rint(value)
                ? String.format(Locale.US, "%.0f", value) : String.format(Locale.US, "%.1f", value);
    }
}
