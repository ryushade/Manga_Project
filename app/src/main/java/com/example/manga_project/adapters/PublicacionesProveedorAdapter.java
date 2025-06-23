package com.example.manga_project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.Modelos.PublicacionProveedor;
import com.example.manga_project.R;
import com.squareup.picasso.Picasso;
import java.util.List;

public class PublicacionesProveedorAdapter extends RecyclerView.Adapter<PublicacionesProveedorAdapter.ViewHolder> {
    private final List<PublicacionProveedor> publicaciones;

    public PublicacionesProveedorAdapter(List<PublicacionProveedor> publicaciones) {
        this.publicaciones = publicaciones;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_publicacion_proveedor, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PublicacionProveedor pub = publicaciones.get(position);
        holder.tvTitulo.setText(pub.titulo);
        holder.tvDescripcion.setText(pub.descripcion);
        if (pub.portada_url != null && !pub.portada_url.isEmpty()) {
            Picasso.get().load(pub.portada_url).placeholder(R.drawable.bg_img_placeholder).into(holder.imgPortada);
        } else {
            holder.imgPortada.setImageResource(R.drawable.bg_img_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return publicaciones.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPortada;
        TextView tvTitulo, tvDescripcion;
        ViewHolder(View v) {
            super(v);
            imgPortada = v.findViewById(R.id.imgPortada);
            tvTitulo = v.findViewById(R.id.tvTitulo);
            tvDescripcion = v.findViewById(R.id.tvDescripcion);
        }
    }
}

