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
import com.example.manga_project.Modelos.ItemUsuario;
import java.util.ArrayList;
import java.util.List;

public class ItemUsuarioAdapter extends RecyclerView.Adapter<ItemUsuarioAdapter.ViewHolder> {
    private List<ItemUsuario> items = new ArrayList<>();
    private OnItemClickListener listener;

    public void setItems(List<ItemUsuario> newItems, boolean isWishlist) {
        items.clear();
        if (newItems != null) {
            for (ItemUsuario item : newItems) {
                item.esWishlist = isWishlist;
                items.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Selecciona el layout según el tipo de lista
        int layoutId = R.layout.item_manga_comic;
        if (viewType == 1) layoutId = R.layout.item_mis_listas;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        // 0 = compras, 1 = wishlist
        return (items.get(position).esWishlist) ? 1 : 0;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemUsuario item = items.get(position);
        holder.tvTitle.setText(item.titulo);
        holder.tvAuthor.setText(item.autores);
        Glide.with(holder.itemView.getContext())
                .load(item.portada)
                .placeholder(R.drawable.ic_placeholder_portada)
                .into(holder.ivCover);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onItemClick(ItemUsuario item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvAuthor;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
        }
    }
}
