package com.yurii.pavlenko.myassistant.flights.report;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.yurii.pavlenko.myassistant.flights.logic.MapProjection;
import com.yurii.pavlenko.myassistant.flights.logic.TileGrid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The map tiles behind a route. The bitmaps are not recycled, because the PDF canvas still reads them
 * when the document is written. Fetching stops at the first failure, so a missing connection costs one attempt.
 */
final class TileLayer {

    private static final double TILE_PIXELS_PER_POINT = 1.5;
    private static final int MAX_TILES = 20;
    private static final float SEAM = 0.4f;

    private final List<TileGrid.Tile> tiles = new ArrayList<>();
    private final List<Bitmap> bitmaps = new ArrayList<>();
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private boolean complete;

    private TileLayer() {
    }

    /** A layer with no tiles, for a report made without a map source. */
    static TileLayer none() {
        return new TileLayer();
    }

    static TileLayer load(TileSource source, MapProjection projection) {
        TileLayer layer = new TileLayer();
        List<TileGrid.Tile> wanted = TileGrid.cover(projection.getWest(), projection.getEast(),
                projection.getSouth(), projection.getNorth(), projection.metresPerPixel(),
                TILE_PIXELS_PER_POINT, MAX_TILES);
        layer.complete = true;
        for (TileGrid.Tile tile : wanted) {
            try {
                layer.bitmaps.add(source.tile(tile));
                layer.tiles.add(tile);
            } catch (IOException | RuntimeException e) {
                layer.complete = false;
                break;
            }
        }
        return layer;
    }

    /** True when every tile of the area was fetched. */
    boolean isComplete() {
        return complete;
    }

    boolean hasTiles() {
        return !tiles.isEmpty();
    }

    void draw(Canvas canvas, MapProjection projection) {
        for (int i = 0; i < tiles.size(); i++) {
            TileGrid.Tile tile = tiles.get(i);
            canvas.drawBitmap(bitmaps.get(i), null, new RectF(
                    (float) projection.x(tile.getWest()) - SEAM, (float) projection.y(tile.getNorth()) - SEAM,
                    (float) projection.x(tile.getEast()) + SEAM, (float) projection.y(tile.getSouth()) + SEAM), paint);
        }
    }
}
