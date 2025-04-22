package com.faisal.rsas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.faisal.rsas.adapter.PasienAdapter;
import com.faisal.rsas.api.ApiClient;
import com.faisal.rsas.api.ApiService;
import com.faisal.rsas.model.Pasien;
import com.faisal.rsas.response.PasienResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private String token,nama;
    private RecyclerView recyclerView;
    private PasienAdapter pasienAdapter;
    private EditText searchEditText;
    private ApiService apiService;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        token = preferences.getString("token", null);
        nama = preferences.getString("nama", "User");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(nama);
        }
        if (token == null) {
            Toast.makeText(this, "Silakan login dulu", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseBody> call = apiService.validateToken("Bearer " + token);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    preferences.edit().clear().apply();
                    Toast.makeText(MainActivity.this, "Token expired, silakan login kembali", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else {
                    initUI();
                    fetchPasienData();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Gagal memverifikasi token: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                preferences.edit().clear().apply();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void initUI() {
        recyclerView = findViewById(R.id.patientRecyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchPasienData();
            }
        });
    }

    private void fetchPasienData() {
        swipeRefreshLayout.setRefreshing(true);

        Call<PasienResponse> call = apiService.getPasien("Bearer " + token);
        call.enqueue(new Callback<PasienResponse>() {
            @Override
            public void onResponse(Call<PasienResponse> call, Response<PasienResponse> response) {
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Pasien> pasienList = response.body().getData();
                    pasienAdapter = new PasienAdapter(MainActivity.this, pasienList);
                    recyclerView.setAdapter(pasienAdapter);

                    searchEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            pasienAdapter.filter(s.toString());
                        }

                        @Override
                        public void afterTextChanged(Editable s) {}
                    });

                } else {
                    Toast.makeText(MainActivity.this, "Gagal ambil data pasien", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PasienResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("API_ERROR", t.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_beranda, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_ganti_password) {
            Toast.makeText(this, "Ganti Password dipilih", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.menu_logout) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("token");
            editor.remove("kd_peg");
            editor.remove("nama");
            editor.apply();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
