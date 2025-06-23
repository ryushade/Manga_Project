package com.example.manga_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.AprobarProveedorRequest;
import com.example.manga_project.Modelos.RechazarProveedorRequest;
import com.example.manga_project.Modelos.SolicitudResponse;
import com.example.manga_project.Modelos.SolicitudesProveedorRequest;
import com.example.manga_project.Modelos.SolicitudesProveedorResponse;
import com.example.manga_project.R;
import com.example.manga_project.adapters.SolicitudesProveedorAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SolicitudesProveedorFragment extends Fragment {

    private RecyclerView recyclerView;
    private SolicitudesProveedorAdapter adapter;
    private AuthService authService;
    private TextView tvSinSolicitudes;

    public SolicitudesProveedorFragment() {
        // Constructor vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_solicitudes_proveedor, container, false);

        recyclerView = view.findViewById(R.id.recyclerSolicitudes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tvSinSolicitudes = view.findViewById(R.id.tvSinSolicitudes);
        tvSinSolicitudes.setVisibility(View.GONE);

        Retrofit retrofit = ApiClient.getClientConToken();
        authService = retrofit.create(AuthService.class);

        cargarSolicitudes();

        return view;
    }

    private void cargarSolicitudes() {
        authService.obtenerSolicitudesProveedor()
                .enqueue(new Callback<SolicitudesProveedorResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SolicitudesProveedorResponse> call,
                                           @NonNull Response<SolicitudesProveedorResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<SolicitudesProveedorRequest> lista = response.body().getData();
                            if (lista != null && !lista.isEmpty()) {
                                adapter = new SolicitudesProveedorAdapter(
                                        getContext(),
                                        lista,
                                        new SolicitudesProveedorAdapter.OnActionClickListener() {
                                            @Override
                                            public void onAprobarClick(SolicitudesProveedorRequest s) {
                                                aprobarSolicitud(s.getId_user());
                                            }
                                            @Override
                                            public void onRechazarClick(SolicitudesProveedorRequest s) {
                                                rechazarSolicitud(s.getId_user());
                                            }
                                        }
                                );
                                recyclerView.setAdapter(adapter);
                                tvSinSolicitudes.setVisibility(View.GONE);
                            } else {
                                tvSinSolicitudes.setVisibility(View.VISIBLE);
                                Toast.makeText(getContext(),
                                        "No hay solicitudes pendientes",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al obtener solicitudes",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SolicitudesProveedorResponse> call,
                                          @NonNull Throwable t) {
                        Toast.makeText(getContext(),
                                "Error al cargar solicitudes: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void aprobarSolicitud(int idUser) {
        new AlertDialog.Builder(getContext())
                .setTitle("¿Aprobar solicitud?")
                .setMessage("¿Estás seguro de que deseas aprobar a este usuario como proveedor?")
                .setPositiveButton("Sí, aprobar", (dialog, which) -> {
                    authService.aprobarProveedor(new AprobarProveedorRequest(idUser))
                            .enqueue(new Callback<SolicitudResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<SolicitudResponse> call,
                                                       @NonNull Response<SolicitudResponse> resp) {
                                    if (resp.isSuccessful() && resp.body() != null) {
                                        Toast.makeText(getContext(),
                                                resp.body().getMsg(),
                                                Toast.LENGTH_SHORT).show();
                                        adapter.eliminarSolicitud(idUser);
                                    } else {
                                        Toast.makeText(getContext(),
                                                "Error al aprobar",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(@NonNull Call<SolicitudResponse> call,
                                                      @NonNull Throwable t) {
                                    Toast.makeText(getContext(),
                                            "Error al aprobar: " + t.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void rechazarSolicitud(int idUser) {
        new AlertDialog.Builder(getContext())
                .setTitle("¿Rechazar solicitud?")
                .setMessage("¿Estás seguro de que deseas rechazar a este usuario como proveedor?")
                .setPositiveButton("Sí, rechazar", (dialog, which) -> {
                    authService.rechazarProveedor(new RechazarProveedorRequest(idUser))
                            .enqueue(new Callback<SolicitudResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<SolicitudResponse> call,
                                                       @NonNull Response<SolicitudResponse> resp) {
                                    if (resp.isSuccessful() && resp.body() != null) {
                                        Toast.makeText(getContext(),
                                                resp.body().getMsg(),
                                                Toast.LENGTH_SHORT).show();
                                        adapter.eliminarSolicitud(idUser);
                                    } else {
                                        Toast.makeText(getContext(),
                                                "Error al rechazar",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(@NonNull Call<SolicitudResponse> call,
                                                      @NonNull Throwable t) {
                                    Toast.makeText(getContext(),
                                            "Error al rechazar: " + t.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
