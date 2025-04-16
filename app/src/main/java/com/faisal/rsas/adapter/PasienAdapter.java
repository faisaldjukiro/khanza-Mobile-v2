package com.faisal.rsas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.faisal.rsas.R;
import com.faisal.rsas.model.Pasien;

import java.util.ArrayList;
import java.util.List;

public class PasienAdapter extends RecyclerView.Adapter<PasienAdapter.ViewHolder> {
    private List<Pasien> pasienList;
    private List<Pasien> originalList;

    public PasienAdapter(List<Pasien> pasienList) {
        this.pasienList = new ArrayList<>(pasienList);
        this.originalList = new ArrayList<>(pasienList);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pasien, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pasien pasien = pasienList.get(position);
        holder.tvNama.setText(pasien.getNm_pasien());
        holder.tvKamar.setText("Kamar      : " + pasien.getKamar());
        holder.tvUmur.setText("Umur        : " + pasien.getUmur() + " Tahun");
        holder.tvNocm.setText(pasien.getNocm());
        holder.tvNorawat.setText("No Rawat : " + pasien.getNo_rawat());
    }

    @Override
    public int getItemCount() {
        return pasienList.size();
    }

    public void filter(String keyword) {
        keyword = keyword.toLowerCase();
        pasienList.clear();

        if (keyword.isEmpty()) {
            pasienList.addAll(originalList);
        } else {
            for (Pasien pasien : originalList) {
                if (pasien.getNm_pasien().toLowerCase().contains(keyword) ||
                        pasien.getNocm().toLowerCase().contains(keyword)) {
                    pasienList.add(pasien);
                }
            }
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvKamar, tvUmur, tvNocm, tvNorawat;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNama);
            tvKamar = itemView.findViewById(R.id.tvKamar);
            tvUmur = itemView.findViewById(R.id.tvUmur);
            tvNocm = itemView.findViewById(R.id.tvNocm);
            tvNorawat = itemView.findViewById(R.id.tvNorawat);
        }
    }
}