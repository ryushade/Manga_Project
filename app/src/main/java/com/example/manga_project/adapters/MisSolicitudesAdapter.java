package com.example.manga_project.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Modelos.RespuestaGenerica;
import com.example.manga_project.Modelos.SoliHistorietaProveedorRequest;
import com.example.manga_project.R;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // Mostrar/ocultar el botón X según el estado
        if (h.eliminar != null) {
            if (s.getEstado().equalsIgnoreCase("pendiente")) {
                h.eliminar.setVisibility(View.VISIBLE);
                h.eliminar.setOnClickListener(v -> {
                    new AlertDialog.Builder(ctx)
                        .setTitle("Cancelar solicitud")
                        .setMessage("¿Deseas cancelar esta solicitud de publicación?")
                        .setPositiveButton("Sí", (dialog, which) -> {
                            AuthService api = ApiClient.getClientConToken().create(AuthService.class);
                            api.borrarSolicitudPublicacion(s.getIdSolicitud()).enqueue(new Callback<RespuestaGenerica>() {
                                @Override
                                public void onResponse(Call<RespuestaGenerica> call, Response<RespuestaGenerica> response) {
                                    if (response.isSuccessful() && response.body() != null && (response.body().code == 200 || response.body().code == 1)) {
                                        int position = h.getAdapterPosition();
                                        if (position != RecyclerView.NO_POSITION) {
                                            lista.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, lista.size());
                                        }
                                        Toast.makeText(ctx, "Solicitud cancelada", Toast.LENGTH_SHORT).show();
                                    } else {
                                        int code = response.body() != null ? response.body().code : -1;
                                        String msg = response.body() != null ? response.body().msg : "";
                                        Toast.makeText(ctx, "No se pudo cancelar la solicitud. Código: " + code + ", msg: " + msg, Toast.LENGTH_LONG).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<RespuestaGenerica> call, Throwable t) {
                                    Toast.makeText(ctx, "Error de red", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("No", null)
                        .show();
                });
            } else {
                h.eliminar.setVisibility(View.GONE);
                h.eliminar.setOnClickListener(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView titulo, tipo, fecha, estado;
        ImageView eliminar;
        Holder(View v) {
            super(v);
            titulo = v.findViewById(R.id.tvTituloSolicitud);
            tipo   = v.findViewById(R.id.tvTipoSolicitud);
            fecha  = v.findViewById(R.id.tvFechaSolicitud);
            estado = v.findViewById(R.id.tvEstadoSolicitud);
            eliminar = v.findViewById(R.id.ivEliminarSolicitud);
        }
    }
}
