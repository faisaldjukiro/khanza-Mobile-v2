package com.faisal.rsas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.faisal.rsas.api.ApiClient;
import com.faisal.rsas.api.ApiService;
import com.faisal.rsas.model.Pasien;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.gson.Gson;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.text.Rectangle;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageViewActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private PhotoView photoView;
    private DrawingView drawingView;
    private Button toggleModeButton;
    private Button colorPickerButton;
    private Button saveButton;
    private String selectedCategory;
    private String token;
    private String noRawat;
    private String pasienJson;
    private boolean isDrawMode = false;
    private final Matrix currentMatrix = new Matrix();

    private final Map<String, Integer> imageMap = new HashMap<String, Integer>() {{
        put("028", R.drawable.grafikicu);
        put("020", R.drawable.berkaspersetujuancvc);
        put("021", R.drawable.berkaspenolakanintubasi);
        put("022", R.drawable.berkaspenolakanrjp);
        put("023", R.drawable.berkaspenolakanumum);
        put("024", R.drawable.berkaspersetujuancvc);
        put("025", R.drawable.berkaspersetujuanintubasi);
        put("026", R.drawable.berkaspersetujuanrjp);
        put("027", R.drawable.berkaspersetujuanumum);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        token = preferences.getString("token", null);
        pasienJson = getIntent().getStringExtra("pasien");
        Pasien pasien = new Gson().fromJson(pasienJson, Pasien.class);
        if (pasien != null) {
            noRawat = pasien.getNo_rawat();
        }

        photoView = findViewById(R.id.photoView);
        drawingView = findViewById(R.id.drawingView);
        toggleModeButton = findViewById(R.id.toggleModeButton);
        colorPickerButton = findViewById(R.id.colorPickerButton);
        saveButton = findViewById(R.id.saveButton);

        toggleModeButton.setText("Mode: Zoom");
        drawingView.setDrawMode(false);
        photoView.setZoomable(true);

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
            toggleModeButton.setText(isDrawMode ? "Mode: Pen" : "Mode: Zoom");
        });

        photoView.setOnMatrixChangeListener(rect -> drawingView.setImageMatrix(photoView.getImageMatrix()));
        colorPickerButton.setOnClickListener(v -> openColorPickerDialog());
        saveButton.setOnClickListener(v -> showSaveConfirmationDialog());

        new Handler().postDelayed(this::showCategoryDialog, 200);
    }

    private void showCategoryDialog() {
        final String[] categories = {
                "028 - CARDEX DAN GRAFIK", "020 - PENOLAKAN CVC", "021 - PENOLAKAN INTUBASI", "022 - PENOLAKAN RJP", "023 - PENOLAKAN UMUM",
                "024 - PERSETUJUAN CVC", "025 - PERSETUJUAN INTUBASI", "026 - PERSETUJUAN RJP", "027 - PERSETUJUAN UMUM",
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Kategori Gambar")
                .setSingleChoiceItems(categories, -1, (dialog, which) -> {
                    String selected = categories[which];
                    selectedCategory = selected.split(" - ")[0];
                    String selectedName = selected.split(" - ")[1];

                    Toast.makeText(this, "Kategori: " + selectedName, Toast.LENGTH_SHORT).show();

                    Integer resId = imageMap.get(selectedCategory);
                    if (resId != null) {
                        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), resId);
                        Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

                        photoView.setImageBitmap(mutableBitmap);
                        drawingView.init(mutableBitmap);

                    } else {
                        Toast.makeText(this, "Gambar tidak tersedia untuk kategori ini", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                })
                .setCancelable(false)
                .create()
                .show();
    }

    private void showSaveConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Simpan Gambar?")
                .setMessage("Apakah Anda ingin menyimpan gambar ini?")
                .setPositiveButton("Simpan", (dialog, which) -> {
                    if (selectedCategory != null) {
                        sendFileToServer(selectedCategory);
                    } else {
                        Toast.makeText(this, "Kategori belum dipilih", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void openColorPickerDialog() {
        new ColorPickerDialog.Builder(this)
                .setTitle("Pilih Warna")
                .setPreferenceName("ColorPicker")
                .setPositiveButton("Pilih", new ColorEnvelopeListener() {
                    @Override
                    public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                        drawingView.setColor("#" + envelope.getHexCode());
                    }
                })
                .setNegativeButton("Batal", (dialogInterface, i) -> dialogInterface.dismiss())
                .attachAlphaSlideBar(true)
                .attachBrightnessSlideBar(true)
                .show();
    }

    private void sendFileToServer(String kategori) {
        Bitmap bitmap = drawingView.getBitmap();
        File pdfFile = new File(getCacheDir(), "gambar.pdf");

        try {
            Rectangle pageSize;
            if ("005".equals(kategori)) {
                pageSize = com.itextpdf.text.PageSize.LEGAL.rotate();
            } else {
                pageSize = com.itextpdf.text.PageSize.LEGAL;
            }

            com.itextpdf.text.Document document = new com.itextpdf.text.Document(pageSize);
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] imageBytes = stream.toByteArray();

            com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(imageBytes);
            image.scaleToFit(pageSize.getWidth(), pageSize.getHeight());
            image.setAlignment(com.itextpdf.text.Image.ALIGN_CENTER);

            document.add(image);
            document.close();
            uploadPdfToServer(pdfFile, kategori);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membuat PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void uploadPdfToServer(File pdfFile, String kategori) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), pdfFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", pdfFile.getName(), requestFile);
        RequestBody noRawatBody = RequestBody.create(noRawat, MediaType.parse("text/plain"));
        RequestBody kode = RequestBody.create(MediaType.parse("text/plain"), kategori);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.tambahBerkas("Bearer " + token, body, noRawatBody, kode);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ImageViewActivity.this, "Berkas berhasil diunggah", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ImageViewActivity.this, BerkasdigitalActivity.class);
                    intent.putExtra("token", token);
                    intent.putExtra("pasien", pasienJson);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ImageViewActivity.this, "Gagal mengunggah file", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ImageViewActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

