package com.example.manga_project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.manga_project.R;
import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.ItemUsuario;
import com.example.manga_project.Modelos.RespuestaGenerica;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemUsuarioAdapter extends RecyclerView.Adapter<ItemUsuarioAdapter.ViewHolder> {
    private List<ItemUsuario> items = new ArrayList<>();
    private OnItemClickListener listener;
    private OnRefundClickListener refundListener;
    private boolean showRefundButton = false;

    public void setItems(List<ItemUsuario> newItems, boolean isWishlist) {
        items.clear();
        if (newItems != null) {
            for (ItemUsuario item : newItems) {
                item.esWishlist = isWishlist;
                items.add(item);
            }
        }
        showRefundButton = !isWishlist; // Solo mostrar en compras, no en wishlist
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

        // Lógica para eliminar de wishlist
        if (holder.ivEliminarLista != null && item.esWishlist) {
            holder.ivEliminarLista.setVisibility(View.VISIBLE);
            holder.ivEliminarLista.setOnClickListener(v -> {
                AuthService api = ApiClient.getClientConToken().create(AuthService.class);
                api.eliminarWishlist(item.id_volumen).enqueue(new Callback<RespuestaGenerica>() {
                    @Override
                    public void onResponse(Call<RespuestaGenerica> call, Response<RespuestaGenerica> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().code == 0) {
                            int pos = holder.getBindingAdapterPosition();
                            if (pos != RecyclerView.NO_POSITION) {
                                items.remove(pos);
                                notifyItemRemoved(pos);
                                Toast.makeText(holder.itemView.getContext(), "Eliminado de tu lista", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(holder.itemView.getContext(), "No se pudo eliminar", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<RespuestaGenerica> call, Throwable t) {
                        Toast.makeText(holder.itemView.getContext(), "Error de red", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } else if (holder.ivEliminarLista != null) {
            holder.ivEliminarLista.setVisibility(View.GONE);
        }

        // Lógica para botón de devolución
        if (holder.btnRefund != null && showRefundButton && item.puedeDevolver) {
            holder.btnRefund.setVisibility(View.VISIBLE);
            holder.btnRefund.setOnClickListener(v -> {
                if (refundListener != null) {
                    refundListener.onRefundClick(item);
                }
            });
        } else if (holder.btnRefund != null) {
            holder.btnRefund.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onItemClick(ItemUsuario item);
    }

    public interface OnRefundClickListener {
        void onRefundClick(ItemUsuario item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnRefundClickListener(OnRefundClickListener listener) {
        this.refundListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvAuthor;
        ImageView ivEliminarLista;
        Button btnRefund;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            ivEliminarLista = itemView.findViewById(R.id.ivEliminarLista);
            btnRefund = itemView.findViewById(R.id.btnRefund);
        }
    }
}
