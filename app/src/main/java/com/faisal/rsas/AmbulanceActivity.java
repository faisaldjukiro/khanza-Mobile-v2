package com.faisal.rsas;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.faisal.rsas.api.ApiService;
import com.faisal.rsas.model.DirectionsResponse;
import com.faisal.rsas.model.Pasien;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AmbulanceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private SharedPreferences preferences;
    private GoogleMap mMap;
    private AutoCompleteTextView autoCompleteDriver;
    private TextView tvJarak, tvNamaPasien, tvEstimasiBiaya, tvNorawat, tvNocm;
    private Button btnSimpan;
    private final LatLng lokasiRSAS = new LatLng(0.555456, 123.0842965);
    private LatLng lokasiTujuan = null;

    private MaterialButtonToggleGroup toggleCityType, toggleNurse, toggleDokter;
    private boolean isDalamKota = true;
    private boolean adaPerawat = false;
    private int tipeDokter = 0;
    private float jarakKm = 0;

    private final String apiKey = "AIzaSyAsTH1fSpELYpFd_P9tg74JS5JXaYwGlY4";
    private String token;
    private String noRawat;
    private String pasienJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        token = preferences.getString("token", null);
        pasienJson = getIntent().getStringExtra("pasien");

        Pasien pasien = new Gson().fromJson(pasienJson, Pasien.class);
        tvNamaPasien = findViewById(R.id.tvNamaPasien);
        tvNorawat = findViewById(R.id.tvNoRawat);
        tvNocm = findViewById(R.id.tvCm);
        if (pasien != null) {
            if (tvNamaPasien != null) {
                tvNamaPasien.setText(pasien.getNm_pasien());
            }
            if (tvNorawat != null) {
                tvNorawat.setText(pasien.getNo_rawat());
            }
            if (tvNocm != null) {
                tvNocm.setText(pasien.getNocm());
            }
            noRawat = pasien.getNo_rawat();
        }


        autoCompleteDriver = findViewById(R.id.autoCompleteDriver);
        tvJarak = findViewById(R.id.tvJarak);
        tvEstimasiBiaya = findViewById(R.id.tvEstimasiBiaya);
        btnSimpan = findViewById(R.id.btnSimpan);

        toggleCityType = findViewById(R.id.toggleCityType);
        toggleNurse = findViewById(R.id.toggleNurse);
        toggleDokter = findViewById(R.id.toggleDokter);

        setupDriverDropdown();
        setupToggleListeners();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragmentContainer);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnSimpan.setOnClickListener(v -> simpanData());
        initAutoComplete();
    }

    private void setupToggleListeners() {
        toggleCityType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isDalamKota = checkedId == R.id.btnDalamKota;
                hitungDanTampilkanBiaya();
            }
        });

        toggleNurse.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                adaPerawat = checkedId == R.id.btnPerawatYa;
                hitungDanTampilkanBiaya();
            }
        });

        toggleDokter.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnDokterTidak) {
                    tipeDokter = 0;
                } else if (checkedId == R.id.btnDokterUmumYa) {
                    tipeDokter = 1;
                } else if (checkedId == R.id.btnDokterSpesialisYa) {
                    tipeDokter = 2;
                }
                hitungDanTampilkanBiaya();
            }
        });
    }

    private void initAutoComplete() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteFragment.setHint("Cari Lokasi Tujuan");

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    lokasiTujuan = place.getLatLng();
                    if (mMap != null) {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(lokasiTujuan).title("Tujuan: " + place.getName()));
                        mMap.addMarker(new MarkerOptions().position(lokasiRSAS).title("RS Aloei Saboe"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiTujuan, 15f));
                        tampilkanRute();
                        hitungJarakDenganRute();
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(AmbulanceActivity.this, "Error memilih lokasi: " + status, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(lokasiRSAS).title("RS Aloei Saboe"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiRSAS, 15f));
    }

    private void tampilkanRute() {
        if (lokasiTujuan == null || mMap == null) return;

        String origin = lokasiRSAS.latitude + "," + lokasiRSAS.longitude;
        String destination = lokasiTujuan.latitude + "," + lokasiTujuan.longitude;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getDirections(origin, destination, apiKey).enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().routes.isEmpty()) {
                    String encodedPolyline = response.body().routes.get(0).overviewPolyline.points;
                    List<LatLng> decodedPath = com.google.maps.android.PolyUtil.decode(encodedPolyline);

                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(lokasiRSAS).title("RS Aloei Saboe"));
                    mMap.addMarker(new MarkerOptions().position(lokasiTujuan).title("Tujuan"));

                    mMap.addPolyline(new PolylineOptions()
                            .addAll(decodedPath)
                            .color(Color.BLUE)
                            .width(10f));

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (LatLng point : decodedPath) {
                        builder.include(point);
                    }
                    LatLngBounds bounds = builder.build();

                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Toast.makeText(AmbulanceActivity.this, "Gagal mengambil rute: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hitungJarakDenganRute() {
        if (lokasiTujuan == null) return;

        String origin = lokasiRSAS.latitude + "," + lokasiRSAS.longitude;
        String destination = lokasiTujuan.latitude + "," + lokasiTujuan.longitude;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getDirections(origin, destination, apiKey).enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().routes.isEmpty()) {
                    DirectionsResponse.Route.Leg leg = response.body().routes.get(0).legs.get(0);
                    String jarak = leg.distance.text;
                    String durasi = leg.duration.text;
                    tvJarak.setText("Jarak: " + jarak + "\nEstimasi Waktu: " + durasi);

                    jarakKm = leg.distance.value / 1000f;
                    hitungDanTampilkanBiaya();
                } else {
                    tvJarak.setText("Gagal mendapatkan data rute");
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                tvJarak.setText("Error: " + t.getMessage());
            }
        });
    }

    private void hitungDanTampilkanBiaya() {
        double totalBiaya = 0;

        if (isDalamKota) {
            totalBiaya = 75000;
        } else {
            totalBiaya = jarakKm * 10000 * 2;

            if (adaPerawat) {
                totalBiaya += 100000;
            }

            if (tipeDokter == 1) {
                totalBiaya += 750000;
            } else if (tipeDokter == 2) {
                totalBiaya += 1500000;
            }
        }

        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String biayaFormatted = formatRupiah.format(totalBiaya);

        tvEstimasiBiaya.setText("Biaya: " + biayaFormatted);
    }

    private void simpanData() {
        String namaSopir = autoCompleteDriver.getText().toString().trim();
        if (namaSopir.isEmpty()) {
            Toast.makeText(this, "Pilih sopir terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (lokasiTujuan == null) {
            Toast.makeText(this, "Pilih lokasi tujuan terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences pref = getSharedPreferences("DataAmbulance", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("nama_sopir", namaSopir);
        editor.putFloat("jarak_km", jarakKm);
        editor.putBoolean("dalam_kota", isDalamKota);
        editor.putBoolean("ada_perawat", adaPerawat);
        editor.putInt("tipe_dokter", tipeDokter);
        editor.apply();

        Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show();
    }

    private void setupDriverDropdown() {
        HashMap<String, String> driverMap = new HashMap<>();
        driverMap.put("FREDY IBRAHIM", "PEG447");
        driverMap.put("DEDDY SYARIFUDDIN ISMAI", "PEG997");
        driverMap.put("ZULKARNAIN MALAHIKA", "PEG929");
        driverMap.put("ARMAN LAMUDA", "PEG834");
        driverMap.put("JIBRAN AMRAIN", "PEG581");
        driverMap.put("NURSALIM ASSAGAF", "PEG579");
        driverMap.put("SOFYAN LAMOHAMAD", "PEG504");
        driverMap.put("RONI RAJAK", "PEG488");
        driverMap.put("WAHID ABUBAKAR JUSSITHA", "PEG450");
        driverMap.put("HASAN SALEH GANI", "PEG1302");

        List<String> driverNames = new ArrayList<>(driverMap.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                driverNames
        );

        autoCompleteDriver.setAdapter(adapter);
        autoCompleteDriver.setDropDownBackgroundResource(android.R.color.white);

        autoCompleteDriver.setOnClickListener(v -> autoCompleteDriver.showDropDown());
        autoCompleteDriver.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) autoCompleteDriver.showDropDown();
        });

        autoCompleteDriver.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = parent.getItemAtPosition(position).toString();
            String selectedCode = driverMap.get(selectedName);
            Log.d("SelectedDriver", "Nama: " + selectedName + ", Kode: " + selectedCode);
        });
    }
}