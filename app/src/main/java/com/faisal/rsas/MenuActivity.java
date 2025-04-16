package com.faisal.rsas;

import android.os.Bundle;
import android.widget.GridView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.faisal.rsas.adapter.MenuAdapter;
import com.faisal.rsas.item.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        GridView gridMenu = findViewById(R.id.gridMenu);
        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(R.drawable.ic_note, "CPPT / SOAP"));
        menuItems.add(new MenuItem(R.drawable.ic_tindakan, "Tindakan"));
        menuItems.add(new MenuItem(R.drawable.ic_bed, "Input Resep"));
        menuItems.add(new MenuItem(R.drawable.ic_bed, "Laboratorium"));
        menuItems.add(new MenuItem(R.drawable.ic_bed, "Radiologi"));
        menuItems.add(new MenuItem(R.drawable.ic_dokumen, "Berkas Digital"));
        menuItems.add(new MenuItem(R.drawable.ic_bed, "Jadwal Operasi"));
        menuItems.add(new MenuItem(R.drawable.ic_bed, "Edukasi Pasien"));
        menuItems.add(new MenuItem(R.drawable.ic_bed, "Monitoring TTV"));
        menuItems.add(new MenuItem(R.drawable.ic_bed, "Catatan Keperawatan"));
        menuItems.add(new MenuItem(R.drawable.ic_bed, "Catatan Gizi"));

        MenuAdapter adapter = new MenuAdapter(this, menuItems);
        gridMenu.setAdapter(adapter);
    }
}