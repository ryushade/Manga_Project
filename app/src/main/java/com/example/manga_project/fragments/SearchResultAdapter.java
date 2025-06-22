package com.example.manga_project.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.Modelos.BusquedaHistorieta;
import com.example.manga_project.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    private List<BusquedaHistorieta> resultados;

    public SearchResultAdapter(List<BusquedaHistorieta> resultados) {
        this.resultados = resultados;
    }

    public void setResultados(List<BusquedaHistorieta> resultados) {
        this.resultados = resultados;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(BusquedaHistorieta historieta);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusquedaHistorieta item = resultados.get(position);
        holder.tvTitle.setText(item.titulo);
        holder.tvCategory.setText(item.tipo);
        holder.tvSubtitle.setText(item.descripcion);
        if (item.portada_url != null && !item.portada_url.isEmpty()) {
            Picasso.get().load(item.portada_url).placeholder(R.drawable.ic_launcher_foreground).into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.ic_launcher_foreground);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return resultados != null ? resultados.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvCategory, tvTitle, tvSubtitle;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvSubtitle = itemView.findViewById(R.id.tv_subtitle);
        }
    }
}
