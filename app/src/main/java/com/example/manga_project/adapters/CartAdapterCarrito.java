package com.example.manga_project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.ListarCarritoResponse.ItemCarrito;
import com.example.manga_project.R;
import java.util.List;

public class CartAdapterCarrito extends RecyclerView.Adapter<CartAdapterCarrito.ViewHolder> {
    private List<ItemCarrito> items;

    public interface OnEliminarClickListener {
        void onEliminarClick(ItemCarrito item, int position);
    }

    private OnEliminarClickListener eliminarClickListener;
    private AuthService api;

    public CartAdapterCarrito(List<ItemCarrito> items, OnEliminarClickListener listener, AuthService api) {
        this.items = items;
        this.eliminarClickListener = listener;
        this.api = api;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_carrito, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemCarrito item = items.get(position);
        holder.tvTitulo.setText(item.titulo_volumen);
        holder.tvHistorieta.setText(item.historieta);
        holder.tvCantidad.setText("Cantidad: " + item.cantidad);
        holder.tvPrecio.setText(String.format("S/. %.2f", item.precio_unit));
        com.squareup.picasso.Picasso.get().load(item.portada_url).placeholder(R.drawable.ic_placeholder_portada).into(holder.ivPortada);

        holder.deleteButton.setOnClickListener(v -> {
            if (eliminarClickListener != null) {
                eliminarClickListener.onEliminarClick(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPortada;
        TextView tvTitulo, tvHistorieta, tvCantidad, tvPrecio;
        Button deleteButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPortada = itemView.findViewById(R.id.ivPortada);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvHistorieta = itemView.findViewById(R.id.tvHistorieta);
            tvCantidad = itemView.findViewById(R.id.tvCantidad);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
