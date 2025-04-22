package com.faisal.rsas;

import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.faisal.rsas.adapter.MenuAdapter;
import com.faisal.rsas.item.MenuItem;
import com.faisal.rsas.model.Pasien;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        String pasienJson = getIntent().getStringExtra("pasien");
        Pasien pasien = new Gson().fromJson(pasienJson, Pasien.class);

        TextView tvNamaPasien = findViewById(R.id.tvNamaPasien);
        if (pasien != null && tvNamaPasien != null) {
            tvNamaPasien.setText(pasien.getNm_pasien());
        }

        GridView gridMenu = findViewById(R.id.gridMenu);
        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(R.drawable.ic_note, "CPPT / SOAP"));
        menuItems.add(new MenuItem(R.drawable.ic_dokumen, "Berkas Digital"));
        menuItems.add(new MenuItem(R.drawable.ic_tindakan, "Tindakan"));
        menuItems.add(new MenuItem(R.drawable.ic_obat, "Input Resep"));
        menuItems.add(new MenuItem(R.drawable.ic_lab, "Laboratorium"));
        menuItems.add(new MenuItem(R.drawable.ic_radiologi, "Radiologi"));
        menuItems.add(new MenuItem(R.drawable.ic_operasi, "Jadwal Operasi"));
        menuItems.add(new MenuItem(R.drawable.ic_edukasi, "Edukasi Pasien"));
        menuItems.add(new MenuItem(R.drawable.ic_ttv, "Monitoring TTV"));
        menuItems.add(new MenuItem(R.drawable.ic_note, "Catatan Keperawatan"));
        menuItems.add(new MenuItem(R.drawable.ic_note, "Catatan Gizi"));

        MenuAdapter adapter = new MenuAdapter(this, menuItems, pasien);
        gridMenu.setAdapter(adapter);
    }
}