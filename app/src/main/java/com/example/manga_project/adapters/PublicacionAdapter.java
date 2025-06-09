package com.example.manga_project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.manga_project.R;
import com.example.manga_project.Modelos.PublicacionItem;

import java.util.List;

public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.ViewHolder> {

    private final List<PublicacionItem> lista;
    private final Context context;

    public PublicacionAdapter(Context context, List<PublicacionItem> lista) {
        this.context = context;
        this.lista = lista;
    }

    @NonNull
    @Override
    public PublicacionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_solicitud_publicacion_proveedor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublicacionAdapter.ViewHolder holder, int position) {
        PublicacionItem item = lista.get(position);

        holder.tvTitulo.setText("Título: " + item.getTitulo());
        holder.tvTipo.setText("Tipo: " + item.getTipo());
        holder.tvAutor.setText("Autor: " + item.getAutor());
        holder.tvCorreo.setText("Contacto: " + item.getCorreoContacto());
        holder.tvFecha.setText("Solicitado: " + item.getFechaSolicitud());

        Glide.with(context)
                .load(item.getUrlPortada())
                .placeholder(R.drawable.ic_placeholder_portada)
                .into(holder.imgPortada);

        holder.btnVerContenido.setOnClickListener(v -> {
            Toast.makeText(context, "Ver contenido de: " + item.getTitulo(), Toast.LENGTH_SHORT).show();
            // Aquí puedes abrir un Dialog o ir a una nueva activity
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPortada;
        TextView tvTitulo, tvTipo, tvAutor, tvCorreo, tvFecha;
        Button btnVerContenido;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPortada = itemView.findViewById(R.id.img_portada_historieta);
            tvTitulo = itemView.findViewById(R.id.tv_titulo_historieta);
            tvTipo = itemView.findViewById(R.id.tv_tipo_historieta);
            tvAutor = itemView.findViewById(R.id.tv_autor_historieta);
            tvCorreo = itemView.findViewById(R.id.tv_correo_contacto);
            tvFecha = itemView.findViewById(R.id.tv_fecha_solicitud);
            btnVerContenido = itemView.findViewById(R.id.btn_ver_contenido);
        }
    }
}
