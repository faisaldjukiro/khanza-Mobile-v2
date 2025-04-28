package com.faisal.rsas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
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
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.io.File;
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
    private Button saveButton; // Tombol simpan
    private String selectedCategory;
    private String token;
    private String noRawat;
    private String pasienJson;

    private boolean isDrawMode = false;
    private final Matrix currentMatrix = new Matrix();

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
        saveButton = findViewById(R.id.saveButton); // Tombol simpan

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.grafikicu)
                .copy(Bitmap.Config.ARGB_8888, true);
        photoView.setImageBitmap(bitmap);
        drawingView.init(bitmap);
        photoView.setZoomable(true);
        drawingView.setDrawMode(false);
        toggleModeButton.setText("Mode: Zoom");

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

        // Save Button Click Listener
        saveButton.setOnClickListener(v -> showCategoryDialog());
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

    private void showCategoryDialog() {
        final String[] categories = {
                "001 - Berkas SEP", "002 - KTP", "003 - Kartu Keluarga", "004 - Kartu Pasien",
                "005 - Berkas Digital", "006 - Bukti Visum", "007 - Gambar EKG", "008 - Gambar USG",
                "009 - Foto Rontgen", "010 - Surat Kematian", "011 - Surat Rujukan", "012 - Resume",
                "013 - Laporan Operasi", "014 - Laporan Kepolisian", "015 - Berkas SITB",
                "016 - Resume Pasien Pindah", "017 - Persetujuan Tindakan Medis", "018 - Protokol Pengobatan Kemoterapi",
                "019 - Laporan Jasa Raharja"
        };

        // Map category: Code -> Name
        Map<String, String> categoryMap = new HashMap<>();
        for (String category : categories) {
            String[] parts = category.split(" - ");
            categoryMap.put(parts[0], parts[1]);
        }

        // Dialog for selecting category
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Kategori")
                .setSingleChoiceItems(categories, -1, (dialog, which) -> {
                    // Store the selected category code
                    String selected = categories[which];
                    String selectedCode = selected.split(" - ")[0];
                    String selectedName = selected.split(" - ")[1];

                    selectedCategory = selectedCode;  // The category code to send to the server
                    Toast.makeText(this, "Kategori dipilih: " + selectedName, Toast.LENGTH_SHORT).show();
                })
                .setPositiveButton("Pilih", (dialog, id) -> {
                    if (selectedCategory != null) {

                        sendFileToServer(selectedCategory);
                    } else {
                        Toast.makeText(this, "Pilih kategori terlebih dahulu", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", (dialog, id) -> dialog.dismiss())
                .create()
                .show();
    }

    private void sendFileToServer(String kategori) {
        Bitmap bitmap = drawingView.getBitmap();
        File pdfFile = new File(getCacheDir(), "gambar.pdf");
        try {
            PdfWriter writer = new PdfWriter(pdfFile.getAbsolutePath());
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument, PageSize.A4);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            com.itextpdf.io.image.ImageData imageData = com.itextpdf.io.image.ImageDataFactory.create(byteArray);
            Image image = new Image(imageData);

            image.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
            document.add(image);

            document.close();

            uploadPdfToServer(pdfFile, kategori);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membuat PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadPdfToServer(File pdfFile, String kategori) {
        // Create RequestBody for the PDF file
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
                    Toast.makeText(ImageViewActivity.this, "Berkas Digital Berhasil Di Tambahkan", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ImageViewActivity.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ImageViewActivity.this, "Gagal upload file", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ImageViewActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}

