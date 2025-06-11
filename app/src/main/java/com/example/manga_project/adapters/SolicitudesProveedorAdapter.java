package com.example.manga_project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manga_project.R;
import com.example.manga_project.Modelos.SolicitudesProveedorRequest;

import java.util.List;

public class SolicitudesProveedorAdapter
        extends RecyclerView.Adapter<SolicitudesProveedorAdapter.SolicitudViewHolder> {

    public interface OnActionClickListener {
        void onAprobarClick(SolicitudesProveedorRequest solicitud);
        void onRechazarClick(SolicitudesProveedorRequest solicitud);
    }

    private final List<SolicitudesProveedorRequest> listaSolicitudes;
    private final Context context;
    private final OnActionClickListener listener;

    public SolicitudesProveedorAdapter(Context context,
                                       List<SolicitudesProveedorRequest> listaSolicitudes,
                                       OnActionClickListener listener) {
        this.context = context;
        this.listaSolicitudes = listaSolicitudes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(context)
                .inflate(R.layout.item_solicitud_proveedor, parent, false);
        return new SolicitudViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        SolicitudesProveedorRequest solicitud = listaSolicitudes.get(position);
        holder.tvNombre.setText(solicitud.getNombre());
        holder.tvCorreo.setText(solicitud.getEmail());

        holder.btnAprobar.setOnClickListener(v -> {
            listener.onAprobarClick(solicitud);
        });
        holder.btnRechazar.setOnClickListener(v -> {
            listener.onRechazarClick(solicitud);
        });
    }

    @Override
    public int getItemCount() {
        return listaSolicitudes.size();
    }

    static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCorreo;
        Button btnAprobar, btnRechazar;

        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre   = itemView.findViewById(R.id.tv_nombre_usuario);
            tvCorreo   = itemView.findViewById(R.id.tv_correo_usuario);
            btnAprobar = itemView.findViewById(R.id.btn_aprobar);
            btnRechazar= itemView.findViewById(R.id.btn_rechazar);
        }
    }
}
