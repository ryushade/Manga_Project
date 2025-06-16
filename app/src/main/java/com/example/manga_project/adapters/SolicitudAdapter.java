package com.example.manga_project.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.manga_project.R;
import com.example.manga_project.fragments.RevisionFragment;
import com.example.manga_project.Modelos.Solicitud;

import java.util.List;

public class SolicitudAdapter
        extends RecyclerView.Adapter<SolicitudAdapter.VH> {

    private final List<Solicitud> items;

    public SolicitudAdapter(List<Solicitud> items) {
        this.items = items;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_solicitud_publicacion_proveedor, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override public int getItemCount() {
        return items.size();
    }

    class VH extends RecyclerView.ViewHolder {
        ImageView imgPortada;
        TextView  tvTitulo,
                tvTipo,
                tvAutor,
                tvContacto,
                tvFecha;
        Button    btnVerContenido;

        VH(@NonNull View itemView) {
            super(itemView);
            imgPortada      = itemView.findViewById(R.id.img_portada_historieta);
            tvTitulo        = itemView.findViewById(R.id.tv_titulo_historieta);
            tvTipo          = itemView.findViewById(R.id.tv_tipo_historieta);
            tvAutor         = itemView.findViewById(R.id.tv_autor_historieta);
            tvContacto      = itemView.findViewById(R.id.tv_correo_contacto);
            tvFecha         = itemView.findViewById(R.id.tv_fecha_solicitud);
            btnVerContenido = itemView.findViewById(R.id.btn_ver_contenido);
        }

        void bind(Solicitud s) {
            // Carga de imagen con Glide (si tienes URL)
            Glide.with(imgPortada.getContext())
                    .load(s.getUrl_portada())
                    .placeholder(R.drawable.ic_placeholder_portada)
                    .into(imgPortada);

            tvTitulo   .setText("Título: "  + s.getTitulo());
            tvTipo     .setText("Tipo: "    + s.getTipo());
            tvAutor    .setText("Autor: "   + s.getAutores());
            tvContacto .setText("Contacto: "+ s.getEmail());
            tvFecha    .setText("Solicitado: " + s.getFecha_solicitud());

            btnVerContenido.setOnClickListener(v -> {
                // Prepara el fragment y sus argumentos
                RevisionFragment frag = new RevisionFragment();
                Bundle args = new Bundle();
                args.putInt("ID_SOLICITUD", s.getId_solicitud());
                frag.setArguments(args);

                // Lanza el fragment sobre el contenedor de tu Activity
                AppCompatActivity act = (AppCompatActivity) v.getContext();
                FragmentTransaction tx = act.getSupportFragmentManager().beginTransaction();
                tx.replace(R.id.nav_host_fragment_content_main_admin, frag);
                tx.addToBackStack(null);
                tx.commit();
            });
        }
    }
}

