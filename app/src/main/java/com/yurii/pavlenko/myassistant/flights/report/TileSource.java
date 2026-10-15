package com.yurii.pavlenko.myassistant.flights.report;

import android.graphics.Bitmap;

import com.yurii.pavlenko.myassistant.flights.logic.TileGrid;

import java.io.IOException;

/** Gives the picture of a map tile. */
interface TileSource {

    /** The credit that has to appear on a page that shows these tiles. */
    String attribution();

    /** @throws IOException when the tile cannot be had, for example without a connection */
    Bitmap tile(TileGrid.Tile tile) throws IOException;
}
