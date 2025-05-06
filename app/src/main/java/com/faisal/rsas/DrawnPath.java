package com.faisal.rsas;

import android.graphics.Paint;
import android.graphics.Path;

public class DrawnPath {
    public Path path;
    public Paint paint;

    public DrawnPath(Path path, Paint paint) {
        this.path = path;
        this.paint = paint;
    }
}