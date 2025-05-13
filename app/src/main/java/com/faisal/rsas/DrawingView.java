package com.faisal.rsas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private Bitmap baseBitmap;
    private Canvas drawCanvas;
    private Paint paint;
    private Path path;
    private boolean isDrawMode = false;
    private Matrix inverseMatrix = new Matrix();
    private long downTime;
    private List<PointF> currentStroke = new ArrayList<>();
    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(4f);
    }

    public void setColor(String color) {
        paint.setColor(Color.parseColor(color));
    }

    public void init(Bitmap targetBitmap) {
        baseBitmap = targetBitmap;
        drawCanvas = new Canvas(baseBitmap);
    }

    public void setDrawMode(boolean drawMode) {
        this.isDrawMode = drawMode;
    }

    public void setImageMatrix(Matrix matrix) {
        matrix.invert(inverseMatrix);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isDrawMode) return false;

//        if (event.getToolType(0) != MotionEvent.TOOL_TYPE_STYLUS) {
//            return false;
//        }

        float[] pts = new float[]{event.getX(), event.getY()};
        inverseMatrix.mapPoints(pts);
        float x = pts[0];
        float y = pts[1];

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downTime = event.getEventTime();
                path.reset();
                path.moveTo(x, y);
                currentStroke.clear();
                currentStroke.add(new PointF(x, y));
                break;

            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                drawCanvas.drawPath(path, paint);
                path.reset();
                path.moveTo(x, y);
                currentStroke.add(new PointF(x, y));
                break;

            case MotionEvent.ACTION_UP:
                long duration = event.getEventTime() - downTime;

                currentStroke.add(new PointF(x, y));

                if (isCircle(currentStroke)) {
                    PointF center = getCenter(currentStroke);
                    float radius = getAverageRadius(currentStroke, center);
                    drawCanvas.drawCircle(center.x, center.y, radius, paint);
                } else {
                    path.lineTo(x, y);
                    drawCanvas.drawPath(path, paint);
                }

                path.reset();
                break;
        }

        invalidate();
        return true;
    }

    public Bitmap getBitmap() {
        return baseBitmap;
    }
    private boolean isCircle(List<PointF> points) {
        if (points.size() < 10) return false;

        PointF center = getCenter(points);
        float avgRadius = getAverageRadius(points, center);
        float tolerance = avgRadius * 0.35f;

        float distStartEnd = distance(points.get(0), points.get(points.size() - 1));
        if (distStartEnd > avgRadius * 0.5f) return false;

        for (PointF p : points) {
            float dist = distance(p, center);
            if (Math.abs(dist - avgRadius) > tolerance) {
                return false;
            }
        }

        return true;
    }


    private PointF getCenter(List<PointF> points) {
        float sumX = 0, sumY = 0;
        for (PointF p : points) {
            sumX += p.x;
            sumY += p.y;
        }
        return new PointF(sumX / points.size(), sumY / points.size());
    }

    private float getAverageRadius(List<PointF> points, PointF center) {
        float sum = 0;
        for (PointF p : points) {
            sum += distance(p, center);
        }
        return sum / points.size();
    }

    private float distance(PointF a, PointF b) {
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}