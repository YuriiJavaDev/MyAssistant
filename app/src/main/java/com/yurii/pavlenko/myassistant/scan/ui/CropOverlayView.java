package com.yurii.pavlenko.myassistant.scan.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.yurii.pavlenko.myassistant.R;
import com.yurii.pavlenko.myassistant.scan.imaging.Quad;

/**
 * Shows the photo with a draggable page outline: four corner handles and four edge handles,
 * the latter moving a whole edge, which is how the margins are adjusted by hand.
 */
public class CropOverlayView extends View {

    public interface OnQuadChangedListener {
        void onQuadChanged(Quad quad);
    }

    private static final int CORNER_HANDLES = Quad.CORNER_COUNT;
    private static final int HANDLE_COUNT = CORNER_HANDLES * 2;
    private static final double MIN_AREA = 0.02;

    private final Paint dimPaint = new Paint();
    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handleFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handleStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path dimPath = new Path();
    private final RectF imageRect = new RectF();
    private final float handleRadius;
    private final float touchRadius;

    private Bitmap bitmap;
    private Quad quad;
    private OnQuadChangedListener listener;
    private int activeHandle = -1;
    private float lastX;
    private float lastY;

    public CropOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        handleRadius = 10 * density;
        touchRadius = 32 * density;

        int accent = ContextCompat.getColor(context, R.color.text_color_btn);
        dimPaint.setColor(0x99000000);
        outlinePaint.setColor(accent);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(2 * density);
        handleFillPaint.setColor(0xFFFFFFFF);
        handleStrokePaint.setColor(accent);
        handleStrokePaint.setStyle(Paint.Style.STROKE);
        handleStrokePaint.setStrokeWidth(2 * density);
    }

    public void setOnQuadChangedListener(OnQuadChangedListener listener) {
        this.listener = listener;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        layoutImageRect();
        invalidate();
    }

    public void setQuad(Quad quad) {
        this.quad = quad;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        layoutImageRect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap == null) {
            return;
        }
        canvas.drawBitmap(bitmap, null, imageRect, null);
        if (quad == null) {
            return;
        }

        float[] outline = outlinePoints();
        dimPath.reset();
        dimPath.setFillType(Path.FillType.EVEN_ODD);
        dimPath.addRect(imageRect, Path.Direction.CW);
        addPolygon(dimPath, outline);
        canvas.drawPath(dimPath, dimPaint);

        Path polygon = new Path();
        addPolygon(polygon, outline);
        canvas.drawPath(polygon, outlinePaint);

        for (int handle = 0; handle < HANDLE_COUNT; handle++) {
            float radius = handle < CORNER_HANDLES ? handleRadius : handleRadius * 0.7f;
            float x = handleX(handle);
            float y = handleY(handle);
            canvas.drawCircle(x, y, radius, handleFillPaint);
            canvas.drawCircle(x, y, radius, handleStrokePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (quad == null || bitmap == null) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                activeHandle = findHandle(event.getX(), event.getY());
                lastX = event.getX();
                lastY = event.getY();
                if (activeHandle >= 0) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return activeHandle >= 0;
            case MotionEvent.ACTION_MOVE:
                if (activeHandle >= 0) {
                    dragActiveHandle((event.getX() - lastX) / imageRect.width(), (event.getY() - lastY) / imageRect.height());
                    lastX = event.getX();
                    lastY = event.getY();
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activeHandle = -1;
                return true;
            default:
                return true;
        }
    }

    private void dragActiveHandle(double dx, double dy) {
        Quad candidate;
        if (activeHandle < CORNER_HANDLES) {
            candidate = quad.withCorner(activeHandle, quad.x(activeHandle) + dx, quad.y(activeHandle) + dy);
        } else {
            int edge = activeHandle - CORNER_HANDLES;
            boolean horizontalEdge = edge % 2 == 0;
            candidate = quad.withEdgeMoved(edge, horizontalEdge ? 0 : dx, horizontalEdge ? dy : 0);
        }
        if (candidate.isConvex() && candidate.area() >= MIN_AREA) {
            quad = candidate;
            invalidate();
            if (listener != null) {
                listener.onQuadChanged(quad);
            }
        }
    }

    /** Corners are preferred over edge handles when both are within reach. */
    private int findHandle(float touchX, float touchY) {
        int nearest = -1;
        float nearestDistance = touchRadius;
        for (int handle = 0; handle < HANDLE_COUNT; handle++) {
            float distance = (float) Math.hypot(handleX(handle) - touchX, handleY(handle) - touchY);
            boolean closer = distance < nearestDistance
                    || (distance == nearestDistance && handle < CORNER_HANDLES);
            if (closer) {
                nearest = handle;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private float handleX(int handle) {
        if (handle < CORNER_HANDLES) {
            return toViewX(quad.x(handle));
        }
        int edge = handle - CORNER_HANDLES;
        return toViewX((quad.x(edge) + quad.x((edge + 1) % CORNER_HANDLES)) / 2);
    }

    private float handleY(int handle) {
        if (handle < CORNER_HANDLES) {
            return toViewY(quad.y(handle));
        }
        int edge = handle - CORNER_HANDLES;
        return toViewY((quad.y(edge) + quad.y((edge + 1) % CORNER_HANDLES)) / 2);
    }

    private float[] outlinePoints() {
        float[] points = new float[CORNER_HANDLES * 2];
        for (int corner = 0; corner < CORNER_HANDLES; corner++) {
            points[corner * 2] = toViewX(quad.x(corner));
            points[corner * 2 + 1] = toViewY(quad.y(corner));
        }
        return points;
    }

    private static void addPolygon(Path path, float[] points) {
        path.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) {
            path.lineTo(points[i], points[i + 1]);
        }
        path.close();
    }

    private float toViewX(double normalized) {
        return (float) (imageRect.left + normalized * imageRect.width());
    }

    private float toViewY(double normalized) {
        return (float) (imageRect.top + normalized * imageRect.height());
    }

    /** Fits the photo into the view, leaving room around it so edge handles stay reachable. */
    private void layoutImageRect() {
        if (bitmap == null || getWidth() == 0 || getHeight() == 0) {
            return;
        }
        float padding = handleRadius * 1.5f;
        float scale = Math.min((getWidth() - 2 * padding) / bitmap.getWidth(), (getHeight() - 2 * padding) / bitmap.getHeight());
        float width = bitmap.getWidth() * scale;
        float height = bitmap.getHeight() * scale;
        imageRect.set((getWidth() - width) / 2, (getHeight() - height) / 2, (getWidth() + width) / 2, (getHeight() + height) / 2);
    }
}
