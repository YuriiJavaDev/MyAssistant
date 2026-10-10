package com.yurii.pavlenko.myassistant.scan.imaging;

/** Builds a fake desk photo: a tilted white sheet with text lines, a red stamp and a shadow gradient on a dark surface. */
final class SyntheticPhoto {

    static final int WIDTH = 1200;
    static final int HEIGHT = 900;

    static final double[] PAGE_X = {400, 930, 890, 360};
    static final double[] PAGE_Y = {60, 100, 850, 810};

    private SyntheticPhoto() {
    }

    static Quad pageCorners() {
        double[] xs = new double[4];
        double[] ys = new double[4];
        for (int i = 0; i < 4; i++) {
            xs[i] = PAGE_X[i] / WIDTH;
            ys[i] = PAGE_Y[i] / HEIGHT;
        }
        return new Quad(xs, ys);
    }

    static PixelImage create() {
        PixelImage image = PixelImage.blank(WIDTH, HEIGHT, Colors.rgb(60, 50, 40));
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                double[] uv = pageCoordinates(x + 0.5, y + 0.5);
                if (uv == null) {
                    continue;
                }
                double shadow = 1 - 0.45 * x / WIDTH;
                image.set(x, y, shade(paperColor(uv[0], uv[1]), shadow));
            }
        }
        return image;
    }

    /** Page-relative (u, v) of a photo point, or null outside the sheet. Solved by Newton iteration on the bilinear patch. */
    static double[] pageCoordinates(double px, double py) {
        double u = 0.5;
        double v = 0.5;
        for (int i = 0; i < 20; i++) {
            double fx = point(PAGE_X, u, v) - px;
            double fy = point(PAGE_Y, u, v) - py;
            double dxu = (1 - v) * (PAGE_X[1] - PAGE_X[0]) + v * (PAGE_X[2] - PAGE_X[3]);
            double dxv = (1 - u) * (PAGE_X[3] - PAGE_X[0]) + u * (PAGE_X[2] - PAGE_X[1]);
            double dyu = (1 - v) * (PAGE_Y[1] - PAGE_Y[0]) + v * (PAGE_Y[2] - PAGE_Y[3]);
            double dyv = (1 - u) * (PAGE_Y[3] - PAGE_Y[0]) + u * (PAGE_Y[2] - PAGE_Y[1]);
            double det = dxu * dyv - dxv * dyu;
            u -= (fx * dyv - fy * dxv) / det;
            v -= (dxu * fy - dyu * fx) / det;
        }
        return u >= 0 && u <= 1 && v >= 0 && v <= 1 ? new double[]{u, v} : null;
    }

    private static double point(double[] c, double u, double v) {
        return (1 - u) * (1 - v) * c[0] + u * (1 - v) * c[1] + u * v * c[2] + (1 - u) * v * c[3];
    }

    static boolean isStamp(double u, double v) {
        return u > 0.65 && u < 0.85 && v > 0.12 && v < 0.20;
    }

    static boolean isText(double u, double v) {
        if (u < 0.1 || u > 0.9 || v < 0.28 || v > 0.9 || (int) (u * 40) % 4 == 3) {
            return false;
        }
        return (v * 100) % 4 < 0.9;
    }

    private static int paperColor(double u, double v) {
        if (isStamp(u, v)) {
            return Colors.rgb(200, 30, 30);
        }
        return isText(u, v) ? Colors.rgb(30, 30, 40) : Colors.rgb(245, 240, 230);
    }

    private static int shade(int argb, double factor) {
        return Colors.rgb((int) (Colors.red(argb) * factor), (int) (Colors.green(argb) * factor), (int) (Colors.blue(argb) * factor));
    }
}
