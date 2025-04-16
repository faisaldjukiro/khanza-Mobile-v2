package com.faisal.rsas;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;

public class ImageViewActivity extends AppCompatActivity {

    private PhotoView photoView;
    private DrawingView drawingView;
    private Button toggleModeButton;

    private boolean isDrawMode = false;
    private final Matrix currentMatrix = new Matrix();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        photoView = findViewById(R.id.photoView);
        drawingView = findViewById(R.id.drawingView);
        toggleModeButton = findViewById(R.id.toggleModeButton);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.grafikicu)
                .copy(Bitmap.Config.ARGB_8888, true);
        photoView.setImageBitmap(bitmap);
        drawingView.init(bitmap);
        photoView.setZoomable(true);
        drawingView.setDrawMode(false);
        toggleModeButton.setText("Mode: Pen");

        toggleModeButton.setOnClickListener(v -> {
            isDrawMode = !isDrawMode;
            drawingView.setDrawMode(isDrawMode);
            if (isDrawMode) {
                currentMatrix.set(photoView.getImageMatrix());
                photoView.setZoomable(false);
                photoView.setImageMatrix(currentMatrix);
                drawingView.setImageMatrix(currentMatrix);
            } else {
                photoView.setZoomable(true);
            }
            toggleModeButton.setText(isDrawMode ? "Mode: Zoom" : "Mode: Pen");
        });
        photoView.setOnMatrixChangeListener(rect -> {
            drawingView.setImageMatrix(photoView.getImageMatrix());
        });
    }
}