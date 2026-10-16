package com.yurii.pavlenko.myassistant.flights.logic;

/** Cuts a polygon to a rectangle, so that only the part that can be seen is drawn. */
public final class PolygonClipper {

    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int TOP = 2;
    private static final int BOTTOM = 3;

    private PolygonClipper() {
    }

    /**
     * @param xy the corners as x, y, x, y...; a polygon that is open is treated as closed
     * @return the corners of the part inside the rectangle in the same layout; empty when nothing is inside
     */
    public static double[] clip(double[] xy, double left, double top, double right, double bottom) {
        double[] current = xy;
        double[] edges = {left, right, top, bottom};
        for (int edge = LEFT; edge <= BOTTOM; edge++) {
            current = clipAgainst(current, edge, edges[edge]);
            if (current.length == 0) {
                break;
            }
        }
        return current;
    }

    private static double[] clipAgainst(double[] xy, int edge, double limit) {
        int count = xy.length / 2;
        double[] out = new double[xy.length * 2 + 8];
        int size = 0;
        for (int i = 0; i < count; i++) {
            double ax = xy[2 * i];
            double ay = xy[2 * i + 1];
            double bx = xy[2 * ((i + 1) % count)];
            double by = xy[2 * ((i + 1) % count) + 1];
            boolean aInside = inside(edge, limit, ax, ay);
            boolean bInside = inside(edge, limit, bx, by);
            if (aInside != bInside) {
                double[] cross = cross(edge, limit, ax, ay, bx, by);
                out[size++] = cross[0];
                out[size++] = cross[1];
            }
            if (bInside) {
                out[size++] = bx;
                out[size++] = by;
            }
        }
        double[] result = new double[size];
        System.arraycopy(out, 0, result, 0, size);
        return result;
    }

    private static boolean inside(int edge, double limit, double x, double y) {
        switch (edge) {
            case LEFT: return x >= limit;
            case RIGHT: return x <= limit;
            case TOP: return y >= limit;
            default: return y <= limit;
        }
    }

    private static double[] cross(int edge, double limit, double ax, double ay, double bx, double by) {
        if (edge == LEFT || edge == RIGHT) {
            return new double[]{limit, ay + (by - ay) * (limit - ax) / (bx - ax)};
        }
        return new double[]{ax + (bx - ax) * (limit - ay) / (by - ay), limit};
    }
}
