package com.faisal.rsas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.faisal.rsas.api.ApiClient;
import com.faisal.rsas.api.ApiService;
import com.faisal.rsas.response.LoginResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        String token = preferences.getString("token", null);

        if (token != null) {
            apiService = ApiClient.getClient().create(ApiService.class);
            Call<ResponseBody> call = apiService.validateToken("Bearer " + token);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        preferences.edit().clear().apply();
                        tampilkanFormLogin();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Gagal cek token: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    tampilkanFormLogin();
                }
            });
        } else {
            tampilkanFormLogin();
        }
    }

    private void tampilkanFormLogin() {
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        apiService = ApiClient.getClient().create(ApiService.class);

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua field!", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<LoginResponse> call = apiService.loginUser(username, password);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String token = response.body().getToken();
                    String kdPeg = response.body().getData().getUser().getUsername();
                    String nama  = response.body().getData().getUser().getNama();

                    SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("token", token);
                    editor.putString("kd_peg", kdPeg);
                    editor.putString("nama", nama);
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Login gagal! Cek kode pegawai/password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                if (t.getMessage() != null && (t.getMessage().contains("Failed to connect") ||
                        t.getMessage().contains("timeout") ||
                        t.getMessage().contains("Unable to resolve host"))) {
                    Toast.makeText(LoginActivity.this, "Server tidak dapat dihubungi. Coba lagi nanti.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Terjadi kesalahan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
