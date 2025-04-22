package com.faisal.rsas.adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.faisal.rsas.PdfViewerActivity;
import com.faisal.rsas.R;
import com.faisal.rsas.item.BerkasItem;

import java.util.List;

public class BerkasAdapter extends RecyclerView.Adapter<BerkasAdapter.ViewHolder> {

    private Context context;
    private List<BerkasItem> berkasList;

    public BerkasAdapter(Context context, List<BerkasItem> berkasList) {
        this.context = context;
        this.berkasList = berkasList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_berkasdigital, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BerkasItem item = berkasList.get(position);
        String fileName = item.getUrl().substring(item.getUrl().lastIndexOf("/") + 1);

        holder.tvNama.setText(item.getNama());
        holder.tvNoRawat.setText(fileName);
        holder.cardView.setOnClickListener(v -> {
            String url = item.getUrl();

            Intent intent = new Intent(context, PdfViewerActivity.class);
            intent.putExtra("pdf_url", url);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return berkasList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvNoRawat;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNama);
            tvNoRawat = itemView.findViewById(R.id.tvNoRawat);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}