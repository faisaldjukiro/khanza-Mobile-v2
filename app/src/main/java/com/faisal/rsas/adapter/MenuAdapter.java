package com.faisal.rsas.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.faisal.rsas.BerkasdigitalActivity;
import com.faisal.rsas.R;
import com.faisal.rsas.item.MenuItem;
import com.faisal.rsas.model.Pasien;
import com.google.gson.Gson;

import java.util.List;

public class MenuAdapter extends BaseAdapter {

    private Context context;
    private List<MenuItem> menuItems;
    private Pasien pasien;

    public MenuAdapter(Context context, List<MenuItem> menuItems, Pasien pasien) {
        this.context = context;
        this.menuItems = menuItems;
        this.pasien = pasien;
    }

    @Override
    public int getCount() {
        return menuItems.size();
    }

    @Override
    public Object getItem(int position) {
        return menuItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_menu, parent, false);
        }

        MenuItem menuItem = menuItems.get(position);

        ImageView iconMenu = convertView.findViewById(R.id.iconMenu);
        TextView textMenu = convertView.findViewById(R.id.textMenu);

        if (iconMenu != null && textMenu != null) {
            iconMenu.setImageResource(menuItem.getIconResId());
            textMenu.setText(menuItem.getMenuTitle());
        }
        convertView.setOnClickListener(v -> {
            if (menuItem.getMenuTitle().equals("Berkas Digital")) {
                Intent intent = new Intent(context, BerkasdigitalActivity.class);

                if (pasien != null) {
                    String pasienJson = new Gson().toJson(pasien);
                    intent.putExtra("pasien", pasienJson);
                }
                context.startActivity(intent);
            }
        });
        return convertView;
    }
}