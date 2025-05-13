package com.faisal.rsas;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.provider.OpenableColumns;

import com.faisal.rsas.api.ApiClient;
import com.faisal.rsas.api.ApiService;
import com.faisal.rsas.model.JenisBerkas;
import com.faisal.rsas.model.Pasien;
import com.google.gson.Gson;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadBerkasActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;

    private SharedPreferences preferences;
    private String token;
    private String noRawat,pasienJson;
    private Uri selectedFileUri;

    private TextView txtNamaPasien, txtNamaFile;
    private Spinner spinnerBerkas;

    private List<JenisBerkas> jenisBerkasList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_berkas);

        preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        token = preferences.getString("token", null);
        pasienJson = getIntent().getStringExtra("pasien");
        txtNamaPasien = findViewById(R.id.txtNamaPasien);
        txtNamaFile = findViewById(R.id.txtNamaFile);
        spinnerBerkas = findViewById(R.id.spinnerBerkas);

        String pasienJson = getIntent().getStringExtra("pasien");
        Pasien pasien = new Gson().fromJson(pasienJson, Pasien.class);
        if (pasien != null) {
            noRawat = pasien.getNo_rawat();
            txtNamaPasien.setText("Nama Pasien: " + pasien.getNm_pasien());
        }

        jenisBerkasList = getJenisBerkasList();
        ArrayAdapter<JenisBerkas> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, jenisBerkasList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBerkas.setAdapter(adapter);

        findViewById(R.id.btnPilihFile).setOnClickListener(v -> pilihFile());

        findViewById(R.id.btnUpload).setOnClickListener(v -> {
            if (selectedFileUri != null) {
                uploadFile();
            } else {
                Toast.makeText(this, "Pilih file terlebih dahulu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pilihFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent, "Pilih file PDF"), PICK_FILE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                String fileName = getFileName(selectedFileUri);
                txtNamaFile.setText(fileName);
            }
        }
    }

    private void uploadFile() {
        try {
            File file = FileUtils.copyToFile(this, selectedFileUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(selectedFileUri)), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            RequestBody noRawatBody = RequestBody.create(MultipartBody.FORM, noRawat);
            JenisBerkas selectedJenis = (JenisBerkas) spinnerBerkas.getSelectedItem();
            RequestBody kodeBody = RequestBody.create(MultipartBody.FORM, selectedJenis.getKode());

            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<ResponseBody> call = apiService.uploadBerkas("Bearer " + token, body, noRawatBody, kodeBody);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(UploadBerkasActivity.this, "Upload berhasil", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UploadBerkasActivity.this, BerkasdigitalActivity.class);
                        intent.putExtra("token", token);
                        intent.putExtra("pasien", pasienJson);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(UploadBerkasActivity.this, "Gagal upload", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(UploadBerkasActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal memproses file", Toast.LENGTH_SHORT).show();
        }
    }


    private List<JenisBerkas> getJenisBerkasList() {
        List<JenisBerkas> list = new ArrayList<>();
        list.add(new JenisBerkas("001", "Berkas SEP"));
        list.add(new JenisBerkas("002", "KTP"));
        list.add(new JenisBerkas("003", "KARTU KELUARGA"));
        list.add(new JenisBerkas("004", "KARTU PASIEN"));
        list.add(new JenisBerkas("005", "BERKAS DIGITAL"));
        list.add(new JenisBerkas("006", "BUKTI VISUM"));
        list.add(new JenisBerkas("007", "GAMBAR EKG"));
        list.add(new JenisBerkas("008", "GAMBAR USG"));
        list.add(new JenisBerkas("009", "FOTO RONTGEN"));
        list.add(new JenisBerkas("010", "SURAT KEMATIAN"));
        list.add(new JenisBerkas("011", "SURAT RUJUKAN"));
        list.add(new JenisBerkas("012", "RESUME"));
        list.add(new JenisBerkas("013", "LAPORAN OPERASI"));
        list.add(new JenisBerkas("014", "LAPORAN KEPOLISIAN"));
        list.add(new JenisBerkas("015", "BERKAS SITB"));
        list.add(new JenisBerkas("016", "RESUME PASIEN PINDAH"));
        list.add(new JenisBerkas("017", "PERSETUJUAN TINDAKAN MEDIK"));
        list.add(new JenisBerkas("018", "PROTOKOL PENGOBATAN KEMOTERAPI"));
        list.add(new JenisBerkas("019", "LAPORAN JASA RAHARJA"));
        list.add(new JenisBerkas("020", "PENOLAKAN CVC"));
        list.add(new JenisBerkas("021", "PENOLAKAN INTUBASI"));
        list.add(new JenisBerkas("022", "PENOLAKAN RJP"));
        list.add(new JenisBerkas("023", "PENOLAKAN UMUM"));
        list.add(new JenisBerkas("024", "PERSETUJUAN CVC"));
        list.add(new JenisBerkas("025", "PERSETUJUAN INTUBASI"));
        list.add(new JenisBerkas("026", "PERSETUJUAN RJP"));
        list.add(new JenisBerkas("027", "PERSETUJUAN UMUM"));
        list.add(new JenisBerkas("028", "CARDEX DAN GRAFIK"));
        return list;
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
}
