package com.faisal.rsas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {

    private Bitmap baseBitmap;
    private Canvas drawCanvas;
    private Paint paint;
    private Path path;
    private boolean isDrawMode = false;
    private Matrix inverseMatrix = new Matrix();

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(2f);
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
        float[] pts = new float[]{event.getX(), event.getY()};
        inverseMatrix.mapPoints(pts);
        float x = pts[0];
        float y = pts[1];

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                drawCanvas.drawPath(path, paint);
                path.reset();
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                path.lineTo(x, y);
                drawCanvas.drawPath(path, paint);
                path.reset();
                break;
        }
        invalidate();
        return true;
    }
    public Bitmap getBitmap() {
        return baseBitmap;
    }
}