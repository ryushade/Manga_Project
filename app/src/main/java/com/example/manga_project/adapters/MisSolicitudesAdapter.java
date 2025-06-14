package com.example.manga_project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.R;
import com.example.manga_project.Modelos.SoliHistorietaProveedorRequest;
import java.util.List;

public class MisSolicitudesAdapter
        extends RecyclerView.Adapter<MisSolicitudesAdapter.Holder> {

    private final Context ctx;
    private final List<SoliHistorietaProveedorRequest> lista;

    public MisSolicitudesAdapter(Context ctx,
                                 List<SoliHistorietaProveedorRequest> lista) {
        this.ctx = ctx;
        this.lista = lista;
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent,
                                     int viewType) {
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.item_solicitud_propia, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        SoliHistorietaProveedorRequest s = lista.get(pos);

        h.titulo.setText(s.getTitulo());
        h.tipo.setText("Tipo: " + s.getTipo());
        h.fecha.setText("Enviada: " + s.getFechaSolicitud());
        h.estado.setText("Estado: " + s.getEstado());

        int color;
        switch (s.getEstado().toLowerCase()) {
            case "aprobado":
                color = ContextCompat.getColor(ctx, R.color.green);
                break;
            case "rechazado":
                color = ContextCompat.getColor(ctx, R.color.red);
                break;
            default:
                color = ContextCompat.getColor(ctx, R.color.amber);
        }
        h.estado.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView titulo, tipo, fecha, estado;
        Holder(View v) {
            super(v);
            titulo = v.findViewById(R.id.tvTituloSolicitud);
            tipo   = v.findViewById(R.id.tvTipoSolicitud);
            fecha  = v.findViewById(R.id.tvFechaSolicitud);
            estado = v.findViewById(R.id.tvEstadoSolicitud);
        }
    }
}
