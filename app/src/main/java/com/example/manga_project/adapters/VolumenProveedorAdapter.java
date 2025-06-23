package com.example.manga_project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.Modelos.VolumenProveedor;
import com.example.manga_project.R;
import java.util.List;

public class VolumenProveedorAdapter extends RecyclerView.Adapter<VolumenProveedorAdapter.ViewHolder> {
    private final List<VolumenProveedor> volumenes;

    public VolumenProveedorAdapter(List<VolumenProveedor> volumenes) {
        this.volumenes = volumenes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_volumen_proveedor, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VolumenProveedor vol = volumenes.get(position);
        holder.tvTitulo.setText(vol.titulo_volumen);
        holder.tvHistorieta.setText(vol.historieta);
        holder.tvPrecio.setText(String.format("S/ %.2f", vol.precio_venta));
        holder.tvFecha.setText(vol.fecha_subida);
    }

    @Override
    public int getItemCount() {
        return volumenes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvHistorieta, tvPrecio, tvFecha;
        ViewHolder(View v) {
            super(v);
            tvTitulo = v.findViewById(R.id.tvTituloVolumen);
            tvHistorieta = v.findViewById(R.id.tvHistorieta);
            tvPrecio = v.findViewById(R.id.tvPrecioVolumen);
            tvFecha = v.findViewById(R.id.tvFechaVolumen);
        }
    }
}

