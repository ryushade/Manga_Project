package com.example.manga_project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.manga_project.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para mostrar capítulos con su thumbnail y número de páginas.
 * Por ahora recibe sólo lista de nombres; el thumbnail lo carga con Glide
 * (placeholder) y el pageCount lo deja vacío.
 */
public class CapituloAdapter
        extends RecyclerView.Adapter<CapituloAdapter.VH> {

    public interface OnChapterClick {
        void onChapterClick(String chapterName);
    }

    private final List<String> chapters = new ArrayList<>();
    private final OnChapterClick listener;

    public CapituloAdapter(List<String> initialData, OnChapterClick listener) {
        this.chapters.addAll(initialData);
        this.listener = listener;
    }

    /** Actualiza la lista de capítulos */
    public void actualizarDatos(List<String> newChapters) {
        chapters.clear();
        if (newChapters != null) chapters.addAll(newChapters);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_capitulo, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String chapterName = chapters.get(position);

        // 1) Título
        holder.tvTitle.setText(chapterName);

        // 2) Pages: por ahora lo dejamos vacío o con un placeholder
        holder.tvPages.setText("");

        // 3) Thumbnail: placeholder genérico
        Glide.with(holder.ivThumb.getContext())
                .load(R.drawable.ic_placeholder_portada)
                .into(holder.ivThumb);

        // 4) Click
        holder.itemView.setOnClickListener(v -> listener.onChapterClick(chapterName));
    }

    @Override public int getItemCount() {
        return chapters.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView  tvTitle, tvPages;

        VH(@NonNull View itemView) {
            super(itemView);
            ivThumb  = itemView.findViewById(R.id.ivChapterThumb);
            tvTitle  = itemView.findViewById(R.id.tvChapterTitle);
            tvPages  = itemView.findViewById(R.id.tvChapterPages);
        }
    }
}
