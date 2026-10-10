package com.yurii.pavlenko.myassistant.scan.imaging;

/**
 * Four document corners in normalized image coordinates (0..1), ordered
 * top-left, top-right, bottom-right, bottom-left. Normalized values keep the quad valid
 * whatever resolution the image is shown or processed at.
 */
public final class Quad {

    public static final int CORNER_COUNT = 4;

    private final double[] xs;
    private final double[] ys;

    public Quad(double[] xs, double[] ys) {
        if (xs.length != CORNER_COUNT || ys.length != CORNER_COUNT) {
            throw new IllegalArgumentException("A quad needs exactly four corners");
        }
        this.xs = xs.clone();
        this.ys = ys.clone();
    }

    /** The whole image shrunk by the given fraction on every side. */
    public static Quad inset(double margin) {
        double far = 1 - margin;
        return new Quad(new double[]{margin, far, far, margin}, new double[]{margin, margin, far, far});
    }

    public double x(int corner) {
        return xs[corner];
    }

    public double y(int corner) {
        return ys[corner];
    }

    public Quad withCorner(int corner, double x, double y) {
        double[] newXs = xs.clone();
        double[] newYs = ys.clone();
        newXs[corner] = clampUnit(x);
        newYs[corner] = clampUnit(y);
        return new Quad(newXs, newYs);
    }

    /** Moves two neighbouring corners together, which is how an edge is dragged. */
    public Quad withEdgeMoved(int edge, double dx, double dy) {
        int first = edge;
        int second = (edge + 1) % CORNER_COUNT;
        return withCorner(first, xs[first] + dx, ys[first] + dy)
                .withCorner(second, xs[second] + dx, ys[second] + dy);
    }

    /** True when the corners form a non-degenerate convex polygon, so a perspective warp is well defined. */
    public boolean isConvex() {
        int sign = 0;
        for (int i = 0; i < CORNER_COUNT; i++) {
            int j = (i + 1) % CORNER_COUNT;
            int k = (i + 2) % CORNER_COUNT;
            double cross = (xs[j] - xs[i]) * (ys[k] - ys[j]) - (ys[j] - ys[i]) * (xs[k] - xs[j]);
            int currentSign = cross > 1e-9 ? 1 : cross < -1e-9 ? -1 : 0;
            if (currentSign == 0 || (sign != 0 && currentSign != sign)) {
                return false;
            }
            sign = currentSign;
        }
        return true;
    }

    /** Polygon area as a fraction of the whole image. */
    public double area() {
        double sum = 0;
        for (int i = 0; i < CORNER_COUNT; i++) {
            int j = (i + 1) % CORNER_COUNT;
            sum += xs[i] * ys[j] - xs[j] * ys[i];
        }
        return Math.abs(sum) / 2;
    }

    /** Length of the edge starting at the given corner for an image of the given pixel size. */
    public double edgeLength(int edge, int imageWidth, int imageHeight) {
        int next = (edge + 1) % CORNER_COUNT;
        double dx = (xs[next] - xs[edge]) * imageWidth;
        double dy = (ys[next] - ys[edge]) * imageHeight;
        return Math.hypot(dx, dy);
    }

    private static double clampUnit(double value) {
        return value < 0 ? 0 : Math.min(value, 1);
    }
}
