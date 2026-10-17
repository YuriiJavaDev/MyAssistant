package com.yurii.pavlenko.myassistant.flights.report;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.yurii.pavlenko.myassistant.flights.logic.MapProjection;
import com.yurii.pavlenko.myassistant.flights.logic.TileGrid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * One layer of map tiles behind a route. The bitmaps are not recycled, because the PDF canvas still reads them
 * when the document is written. Fetching stops at the first failure, so a missing connection costs one attempt.
 */
final class TileLayer {

    private static final double TILE_PIXELS_PER_POINT = 3.0;
    private static final int MAX_TILES = 80;
    private static final int PARALLEL_FETCHES = 8;
    private static final float SEAM = 0.4f;

    private final List<TileGrid.Tile> tiles = new ArrayList<>();
    private final List<Bitmap> bitmaps = new ArrayList<>();
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private boolean complete;

    private TileLayer() {
    }

    /** A layer with no tiles, for a map that could not be fetched. */
    static TileLayer none() {
        return new TileLayer();
    }

    /** The tiles that cover the map, the same for every layer. */
    static List<TileGrid.Tile> tilesFor(MapProjection projection) {
        return TileGrid.cover(projection.getWest(), projection.getEast(), projection.getSouth(), projection.getNorth(),
                projection.metresPerPixel(), TILE_PIXELS_PER_POINT, MAX_TILES);
    }

    static TileLayer load(TileSource source, List<TileGrid.Tile> wanted) {
        TileLayer layer = new TileLayer();
        layer.complete = true;
        ExecutorService pool = Executors.newFixedThreadPool(PARALLEL_FETCHES);
        try {
            List<Future<Bitmap>> results = new ArrayList<>();
            for (TileGrid.Tile tile : wanted) {
                results.add(pool.submit(() -> source.tile(tile)));
            }
            for (int i = 0; i < wanted.size(); i++) {
                Bitmap bitmap = layer.await(results.get(i));
                if (!layer.complete) {
                    break;
                }
                if (bitmap != null) {
                    layer.tiles.add(wanted.get(i));
                    layer.bitmaps.add(bitmap);
                }
            }
        } finally {
            pool.shutdownNow();
        }
        return layer;
    }

    private Bitmap await(Future<Bitmap> result) {
        try {
            return result.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            // the tile could not be fetched
        }
        complete = false;
        return null;
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
