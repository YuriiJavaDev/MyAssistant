package com.yurii.pavlenko.myassistant.scan.imaging;

/** Straightens a quadrilateral region of an image into an upright rectangle. */
public final class PerspectiveWarp {

    private PerspectiveWarp() {
    }

    public static PixelImage warp(PixelImage source, Quad quad, int outputWidth, int outputHeight) {
        double[] h = unitSquareToQuad(quad, source.getWidth(), source.getHeight());
        int[] result = new int[outputWidth * outputHeight];

        for (int y = 0; y < outputHeight; y++) {
            double t = (y + 0.5) / outputHeight;
            for (int x = 0; x < outputWidth; x++) {
                double s = (x + 0.5) / outputWidth;
                double w = h[6] * s + h[7] * t + 1;
                double sourceX = (h[0] * s + h[1] * t + h[2]) / w;
                double sourceY = (h[3] * s + h[4] * t + h[5]) / w;
                result[y * outputWidth + x] = sampleBilinear(source, sourceX - 0.5, sourceY - 0.5);
            }
        }
        return new PixelImage(outputWidth, outputHeight, result);
    }

    /** Projective mapping a..h of the unit square onto the quad (Heckbert's closed form), in source pixels. */
    private static double[] unitSquareToQuad(Quad quad, int width, int height) {
        double x0 = quad.x(0) * width, y0 = quad.y(0) * height;
        double x1 = quad.x(1) * width, y1 = quad.y(1) * height;
        double x2 = quad.x(2) * width, y2 = quad.y(2) * height;
        double x3 = quad.x(3) * width, y3 = quad.y(3) * height;

        double sx = x0 - x1 + x2 - x3;
        double sy = y0 - y1 + y2 - y3;

        if (Math.abs(sx) < 1e-9 && Math.abs(sy) < 1e-9) {
            return new double[]{x1 - x0, x3 - x0, x0, y1 - y0, y3 - y0, y0, 0, 0};
        }

        double dx1 = x1 - x2, dx2 = x3 - x2;
        double dy1 = y1 - y2, dy2 = y3 - y2;
        double denominator = dx1 * dy2 - dx2 * dy1;
        double g = (sx * dy2 - dx2 * sy) / denominator;
        double hh = (dx1 * sy - sx * dy1) / denominator;
        return new double[]{
                x1 - x0 + g * x1, x3 - x0 + hh * x3, x0,
                y1 - y0 + g * y1, y3 - y0 + hh * y3, y0,
                g, hh
        };
    }

    private static int sampleBilinear(PixelImage image, double x, double y) {
        int maxX = image.getWidth() - 1;
        int maxY = image.getHeight() - 1;
        double cx = Math.max(0, Math.min(x, maxX));
        double cy = Math.max(0, Math.min(y, maxY));
        int x0 = (int) cx;
        int y0 = (int) cy;
        int x1 = Math.min(x0 + 1, maxX);
        int y1 = Math.min(y0 + 1, maxY);
        double fx = cx - x0;
        double fy = cy - y0;

        int p00 = image.get(x0, y0);
        int p10 = image.get(x1, y0);
        int p01 = image.get(x0, y1);
        int p11 = image.get(x1, y1);

        return Colors.rgb(
                blend(Colors.red(p00), Colors.red(p10), Colors.red(p01), Colors.red(p11), fx, fy),
                blend(Colors.green(p00), Colors.green(p10), Colors.green(p01), Colors.green(p11), fx, fy),
                blend(Colors.blue(p00), Colors.blue(p10), Colors.blue(p01), Colors.blue(p11), fx, fy)
        );
    }

    private static int blend(int c00, int c10, int c01, int c11, double fx, double fy) {
        double top = c00 + (c10 - c00) * fx;
        double bottom = c01 + (c11 - c01) * fx;
        return (int) Math.round(top + (bottom - top) * fy);
    }
}
