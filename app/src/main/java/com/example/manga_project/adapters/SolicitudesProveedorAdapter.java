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

public class SolicitudesProveedorAdapter extends RecyclerView.Adapter<SolicitudesProveedorAdapter.SolicitudViewHolder> {

    private List<SolicitudesProveedorRequest> listaSolicitudes;
    private Context context;
    private OnAprobarClickListener listener;

    public interface OnAprobarClickListener {
        void onAprobarClick(SolicitudesProveedorRequest solicitud);
    }

    public SolicitudesProveedorAdapter(Context context, List<SolicitudesProveedorRequest> listaSolicitudes, OnAprobarClickListener listener) {
        this.context = context;
        this.listaSolicitudes = listaSolicitudes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(context).inflate(R.layout.item_solicitud_proveedor, parent, false);
        return new SolicitudViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        SolicitudesProveedorRequest solicitud = listaSolicitudes.get(position);
        holder.tvNombre.setText(solicitud.getNombre());
        holder.tvCorreo.setText(solicitud.getEmail());

        holder.btnAprobar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAprobarClick(solicitud);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaSolicitudes.size();
    }

    public static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCorreo;
        Button btnAprobar;

        public SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tv_nombre_usuario);
            tvCorreo = itemView.findViewById(R.id.tv_correo_usuario);
            btnAprobar = itemView.findViewById(R.id.btn_aprobar);
        }
    }
}
