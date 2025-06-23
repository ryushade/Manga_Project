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

    private boolean locked = false;

    private static final int TYPE_CHAPTER = 0;
    private static final int TYPE_LOCKED_MSG = 1;

    // ─────────────────���─────────────────────────────────────────────────
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

    public void setLocked(boolean locked) {
        this.locked = locked;
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

    // ───────── Adapter overrides ──────────────────────���──────────────
    @Override
    public int getItemViewType(int position) {
        if (locked && position == episodios.size()) {
            return TYPE_LOCKED_MSG;
        }
        return TYPE_CHAPTER;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOCKED_MSG) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_locked_message, parent, false);
            return new VH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_capitulo, parent, false);
            return new VH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (getItemViewType(position) == TYPE_LOCKED_MSG) {
            holder.tvTitle.setText("Compra para desbloquear todos los capítulos");
            holder.tvPages.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            return;
        }
        holder.tvTitle.setText(episodios.get(position));
        holder.tvPages.setVisibility(View.GONE);
        // Si está locked y no es el primer capítulo, lo ocultamos (por seguridad extra)
        if (locked && position > 0) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        holder.itemView.setOnClickListener(v -> {
            if (locked && position > 0) {
                // No debería pasar, pero por seguridad
                return;
            }
            if (listener != null) listener.onEpisodeClick(episodios.get(position));
        });
    }

    @Override public int getItemCount() {
        return locked ? episodios.size() + 1 : episodios.size();
    }
}
