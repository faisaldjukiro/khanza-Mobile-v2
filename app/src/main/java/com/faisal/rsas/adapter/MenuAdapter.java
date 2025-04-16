package com.faisal.rsas.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.faisal.rsas.R;
import com.faisal.rsas.item.MenuItem;

import java.util.List;

public class MenuAdapter extends BaseAdapter {

    private Context context;
    private List<MenuItem> menuItems;

    // Constructor
    public MenuAdapter(Context context, List<MenuItem> menuItems) {
        this.context = context;
        this.menuItems = menuItems;
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

    // Menampilkan item menu di GridView
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Jika convertView kosong, buat layout baru
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_menu, parent, false);
        }

        // Mengambil data item menu yang sesuai dengan posisi
        MenuItem menuItem = menuItems.get(position);

        // Menentukan komponen-komponen di dalam layout
        ImageView iconMenu = convertView.findViewById(R.id.iconMenu);
        TextView textMenu = convertView.findViewById(R.id.textMenu);

        // Pastikan icon dan text ter-set dengan benar
        if (iconMenu != null && textMenu != null) {
            iconMenu.setImageResource(menuItem.getIconResId());
            textMenu.setText(menuItem.getMenuTitle());
        }

        return convertView;
    }
}