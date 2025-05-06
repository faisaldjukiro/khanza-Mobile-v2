package com.faisal.rsas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.faisal.rsas.adapter.BerkasAdapter;
import com.faisal.rsas.api.ApiClient;
import com.faisal.rsas.api.ApiService;
import com.faisal.rsas.item.BerkasItem;
import com.faisal.rsas.model.Pasien;
import com.faisal.rsas.response.BerkasResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BerkasdigitalActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private BerkasAdapter adapter;
    private List<BerkasItem> berkasList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText searchEditText;
    private FloatingActionButton fabTambahData;

    private String token;
    private String noRawat;
    private String pasienJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_berkasdigital);

        preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        token = preferences.getString("token", null);
        pasienJson = getIntent().getStringExtra("pasien");
        Pasien pasien = new Gson().fromJson(pasienJson, Pasien.class);
        if (pasien != null) {
            noRawat = pasien.getNo_rawat();
        }

        recyclerView = findViewById(R.id.RecyclerViewBerkasDigital);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BerkasAdapter(this, berkasList);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        searchEditText = findViewById(R.id.searchEditText);
        fabTambahData = findViewById(R.id.fabTambahData);

        loadBerkas();

        swipeRefreshLayout.setOnRefreshListener(this::loadBerkas);

        fabTambahData.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(BerkasdigitalActivity.this);
            builder.setTitle("Pilih Aksi")
                    .setItems(new String[]{"Tulis Berkas Digital", "Upload Berkas Digital"}, (dialog, which) -> {
                        Intent intent;
                        if (which == 0) {
                            intent = new Intent(BerkasdigitalActivity.this, ImageViewActivity.class);
                        } else {
                            intent = new Intent(BerkasdigitalActivity.this, UploadBerkasActivity.class);
                        }
                        intent.putExtra("token", token);
                        intent.putExtra("pasien", pasienJson);
                        startActivity(intent);
                    })
                    .show();
        });
    }

    private void loadBerkas() {
        swipeRefreshLayout.setRefreshing(true);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Map<String, String> body = new HashMap<>();
        body.put("no_rawat", noRawat);
        Call<BerkasResponse> call = apiService.getBerkasList("Bearer " + token, body);

        call.enqueue(new Callback<BerkasResponse>() {
            @Override
            public void onResponse(Call<BerkasResponse> call, Response<BerkasResponse> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    berkasList.clear();
                    berkasList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(BerkasdigitalActivity.this, "Berkas Digital Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                    Log.e("API_ERROR", "Response Code: " + response.code() + " Body: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<BerkasResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(BerkasdigitalActivity.this, "Kesalahan jaringan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}