package com.yurii.pavlenko.myassistant.weather.data;

import java.util.List;

/** Decides whether a search has an obvious answer or the user has to choose. */
public final class PlaceChoice {

    /** The best match must be this many times better known than the runner-up to be taken without asking. */
    private static final double CLEAR_WINNER_RATIO = 2.0;

    private PlaceChoice() {
    }

    /** Returns the place to show, or null when several equally likely places are left to choose from. */
    public static Place obvious(List<Place> ranked) {
        if (ranked.isEmpty()) {
            return null;
        }
        if (ranked.size() == 1) {
            return ranked.get(0);
        }
        Place best = ranked.get(0);
        Place next = ranked.get(1);
        return best.getImportance() >= next.getImportance() * CLEAR_WINNER_RATIO ? best : null;
    }
}
