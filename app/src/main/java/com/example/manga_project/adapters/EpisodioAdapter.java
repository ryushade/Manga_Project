package com.example.manga_project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manga_project.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para mostrar episodios (capítulos, números, issues…) en un
 * RecyclerView.  Recibe únicamente una lista de IDs, por ejemplo
 *  "e001", "e002", …  y notifica al listener cuando el usuario pulsa
 *  uno de ellos.
 */
public class EpisodioAdapter
        extends RecyclerView.Adapter<EpisodioAdapter.VH> {

    /** Callback de selección. */
    public interface OnEpisodeClick {
        void onEpisodeClick(String episodeId);
    }

    private final List<String> episodios = new ArrayList<>();
    private final OnEpisodeClick listener;

    // ───────────────────────────────────────────────────────────────────
    public EpisodioAdapter(List<String> initialData, OnEpisodeClick listener) {
        if (initialData != null) episodios.addAll(initialData);
        this.listener = listener;
    }

    /** Permite refrescar la lista completa. */
    public void actualizarDatos(List<String> nuevos) {
        episodios.clear();
        if (nuevos != null) episodios.addAll(nuevos);
        notifyDataSetChanged();
    }

    // ───────── ViewHolder ─────────────────────────────────────────────
    static class VH extends RecyclerView.ViewHolder {
        final TextView  tvTitle, tvPages;
        VH(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvChapterTitle);
            tvPages = v.findViewById(R.id.tvChapterPages);
        }
    }

    // ───────── Adapter overrides ─────────────────────────────────────
    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_capitulo, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        String epId = episodios.get(pos); // p.e. "c003"

        // Mostrar como "Capítulo 003" (rellenando con ceros si es necesario)
        String numero = epId.length() > 1 ? epId.substring(1) : epId;
        h.tvTitle.setText("Capítulo " + numero);

        h.tvPages.setText("");
        h.itemView.setOnClickListener(v -> listener.onEpisodeClick(epId));
    }

    @Override public int getItemCount() { return episodios.size(); }
}
