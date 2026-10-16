package com.yurii.pavlenko.myassistant.flights.report;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.yurii.pavlenko.myassistant.flights.logic.Coastline;
import com.yurii.pavlenko.myassistant.flights.logic.FlightPoint;
import com.yurii.pavlenko.myassistant.flights.logic.MapProjection;
import com.yurii.pavlenko.myassistant.flights.logic.NiceNumbers;
import com.yurii.pavlenko.myassistant.flights.logic.PolygonClipper;
import com.yurii.pavlenko.myassistant.flights.logic.SpeedUnit;

import java.util.List;
import java.util.Locale;

/**
 * Draws the route of a flight on a map: land and sea from the bundled coastline, which stays as the map when
 * there are no tiles, the tile layers over it, a grid of latitudes and longitudes, the route with its start and end,
 * a scale bar and the north arrow.
 */
final class RouteMapPainter {

    private static final double PADDING = 0.12;
    private static final double MIN_SPAN_METRES = 600;
    private static final int GRID_LINES = 4;
    private static final float CLIP_MARGIN = 30f;
    private static final float TEXT_SIZE = 7.5f;

    private final Coastline coastline;
    private final Paint sea = new Paint();
    private final Paint land = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint coast = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint grid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint route = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint marker = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint casing = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outline = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ink = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelBackground = new Paint();
    private final Path landPath = new Path();
    private final Path routePath = new Path();
    private final RectF labelBounds = new RectF();

    RouteMapPainter(Coastline coastline) {
        this.coastline = coastline;
        sea.setColor(ReportColors.SEA);
        land.setColor(ReportColors.LAND);
        land.setStyle(Paint.Style.FILL);
        landPath.setFillType(Path.FillType.EVEN_ODD);
        coast.setColor(ReportColors.COAST);
        coast.setStyle(Paint.Style.STROKE);
        coast.setStrokeWidth(0.8f);
        grid.setColor(ReportColors.GRATICULE);
        grid.setStyle(Paint.Style.STROKE);
        grid.setStrokeWidth(0.5f);
        route.setColor(ReportColors.ROUTE);
        route.setStyle(Paint.Style.STROKE);
        route.setStrokeWidth(2f);
        route.setStrokeCap(Paint.Cap.ROUND);
        route.setStrokeJoin(Paint.Join.ROUND);
        casing.setColor(0xFFFFFFFF);
        casing.setStyle(Paint.Style.STROKE);
        casing.setStrokeWidth(4f);
        casing.setStrokeCap(Paint.Cap.ROUND);
        casing.setStrokeJoin(Paint.Join.ROUND);
        outline.setColor(0xFFFFFFFF);
        outline.setStyle(Paint.Style.STROKE);
        outline.setStrokeWidth(1.5f);
        ink.setColor(ReportColors.TEXT);
        ink.setStyle(Paint.Style.STROKE);
        ink.setStrokeWidth(1.5f);
        text.setColor(ReportColors.TEXT);
        text.setTextSize(TEXT_SIZE);
        labelBackground.setColor(0xCCFFFFFF);
    }

    /** The projection that shows the whole route inside the area; the points must not be empty. */
    static MapProjection projectionFor(RectF area, List<FlightPoint> points) {
        double[] bounds = boundsOf(points);
        return MapProjection.fit(bounds[0], bounds[1], bounds[2], bounds[3],
                area.width(), area.height(), PADDING, MIN_SPAN_METRES);
    }

    /** Draws the map of the route inside the area, with the projection that {@link #projectionFor} made for it. */
    void draw(Canvas canvas, RectF area, MapProjection projection, List<FlightPoint> points, SpeedUnit units,
              List<TileLayer> layers) {
        canvas.save();
        canvas.clipRect(area);
        canvas.translate(area.left, area.top);
        canvas.drawRect(0, 0, area.width(), area.height(), sea);
        drawLand(canvas, projection, area);
        for (TileLayer layer : layers) {
            layer.draw(canvas, projection);
        }
        drawGrid(canvas, projection, area);
        drawRoute(canvas, projection, points);
        drawScaleBar(canvas, projection, area, units);
        drawNorthArrow(canvas, area);
        canvas.restore();

        coast.setStrokeWidth(1f);
        canvas.drawRect(area, coast);
        coast.setStrokeWidth(0.8f);
    }

    private static double[] boundsOf(List<FlightPoint> points) {
        double minLat = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;
        for (FlightPoint point : points) {
            minLat = Math.min(minLat, point.getLatitude());
            maxLat = Math.max(maxLat, point.getLatitude());
            minLon = Math.min(minLon, point.getLongitude());
            maxLon = Math.max(maxLon, point.getLongitude());
        }
        return new double[]{minLat, maxLat, minLon, maxLon};
    }

    private void drawLand(Canvas canvas, MapProjection projection, RectF area) {
        landPath.reset();
        for (Coastline.Ring ring : coastline.getRings()) {
            if (!ring.touches(projection.getWest(), projection.getEast(), projection.getSouth(), projection.getNorth())) {
                continue;
            }
            double[] xy = new double[ring.size() * 2];
            for (int i = 0; i < ring.size(); i++) {
                xy[2 * i] = projection.x(ring.longitude(i));
                xy[2 * i + 1] = projection.y(ring.latitude(i));
            }
            double[] visible = PolygonClipper.clip(xy, -CLIP_MARGIN, -CLIP_MARGIN,
                    area.width() + CLIP_MARGIN, area.height() + CLIP_MARGIN);
            addPolygon(landPath, visible);
        }
        canvas.drawPath(landPath, land);
        canvas.drawPath(landPath, coast);
    }

    private static void addPolygon(Path path, double[] xy) {
        for (int i = 0; i < xy.length / 2; i++) {
            if (i == 0) {
                path.moveTo((float) xy[0], (float) xy[1]);
            } else {
                path.lineTo((float) xy[2 * i], (float) xy[2 * i + 1]);
            }
        }
        if (xy.length > 0) {
            path.close();
        }
    }

    private void drawGrid(Canvas canvas, MapProjection projection, RectF area) {
        double latStep = NiceNumbers.roundUp((projection.getNorth() - projection.getSouth()) / GRID_LINES);
        double lonStep = NiceNumbers.roundUp((projection.getEast() - projection.getWest()) / GRID_LINES);
        String latFormat = "%." + decimalsFor(latStep) + "f°%s";
        String lonFormat = "%." + decimalsFor(lonStep) + "f°%s";
        for (double lat = Math.ceil(projection.getSouth() / latStep) * latStep; lat <= projection.getNorth(); lat += latStep) {
            float y = (float) projection.y(lat);
            canvas.drawLine(0, y, area.width(), y, grid);
            drawLabel(canvas, String.format(Locale.US, latFormat, Math.abs(lat), lat < 0 ? "S" : "N"), 3, y - 2);
        }
        for (double lon = Math.ceil(projection.getWest() / lonStep) * lonStep; lon <= projection.getEast(); lon += lonStep) {
            float x = (float) projection.x(lon);
            canvas.drawLine(x, 0, x, area.height(), grid);
            drawLabel(canvas, String.format(Locale.US, lonFormat, Math.abs(lon), lon < 0 ? "W" : "E"), x + 3, area.height() - 4);
        }
    }

    private static int decimalsFor(double step) {
        return Math.max(0, (int) Math.ceil(-Math.log10(step) - 1e-9));
    }

    private void drawLabel(Canvas canvas, String label, float x, float baseline) {
        float width = text.measureText(label);
        labelBounds.set(x - 1, baseline - TEXT_SIZE, x + width + 1, baseline + 2);
        canvas.drawRect(labelBounds, labelBackground);
        text.setColor(ReportColors.MUTED);
        canvas.drawText(label, x, baseline, text);
        text.setColor(ReportColors.TEXT);
    }

    private void drawRoute(Canvas canvas, MapProjection projection, List<FlightPoint> points) {
        routePath.reset();
        for (int i = 0; i < points.size(); i++) {
            float x = (float) projection.x(points.get(i).getLongitude());
            float y = (float) projection.y(points.get(i).getLatitude());
            if (i == 0) {
                routePath.moveTo(x, y);
            } else {
                routePath.lineTo(x, y);
            }
        }
        canvas.drawPath(routePath, casing);
        canvas.drawPath(routePath, route);
        FlightPoint first = points.get(0);
        FlightPoint last = points.get(points.size() - 1);
        drawMarker(canvas, (float) projection.x(first.getLongitude()), (float) projection.y(first.getLatitude()), ReportColors.START);
        drawMarker(canvas, (float) projection.x(last.getLongitude()), (float) projection.y(last.getLatitude()), ReportColors.END);
    }

    private void drawMarker(Canvas canvas, float x, float y, int color) {
        marker.setColor(color);
        canvas.drawCircle(x, y, 4.5f, marker);
        canvas.drawCircle(x, y, 4.5f, outline);
    }

    private void drawScaleBar(Canvas canvas, MapProjection projection, RectF area, SpeedUnit units) {
        double perMetre = units.distanceFromMetres(1);
        double longest = projection.metresPerPixel() * area.width() * 0.25 * perMetre;
        double length = NiceNumbers.roundDown(longest);
        float barWidth = (float) (length / perMetre / projection.metresPerPixel());
        float left = area.width() - barWidth - 46;
        float y = area.height() - 22;
        text.setColor(ReportColors.TEXT);
        labelBounds.set(left - 4, y - 12, left + barWidth + 36, y + 6);
        canvas.drawRect(labelBounds, labelBackground);
        canvas.drawLine(left, y, left + barWidth, y, ink);
        canvas.drawLine(left, y - 3, left, y + 3, ink);
        canvas.drawLine(left + barWidth, y - 3, left + barWidth, y + 3, ink);
        canvas.drawText(String.format(Locale.US, length < 1 ? "%.1f %s" : "%.0f %s", length, units.distanceSymbol()),
                left, y - 4, text);
    }

    private void drawNorthArrow(Canvas canvas, RectF area) {
        float x = area.width() - 16;
        float y = 30;
        canvas.drawLine(x, y, x, y - 16, ink);
        canvas.drawLine(x, y - 16, x - 3, y - 10, ink);
        canvas.drawLine(x, y - 16, x + 3, y - 10, ink);
        text.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("N", x, y + 9, text);
        text.setTextAlign(Paint.Align.LEFT);
    }
}
