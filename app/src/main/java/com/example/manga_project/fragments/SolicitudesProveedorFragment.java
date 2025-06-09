package com.example.manga_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.app.AlertDialog;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.AprobarProveedorRequest;
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

    public SolicitudesProveedorFragment() {
        // Constructor vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_solicitudes_proveedor, container, false);
        recyclerView = view.findViewById(R.id.recyclerSolicitudes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Retrofit retrofit = ApiClient.getClientConToken();
        authService = retrofit.create(AuthService.class);

        cargarSolicitudes();

        return view;
    }

    private void cargarSolicitudes() {
        Call<SolicitudesProveedorResponse> call = authService.obtenerSolicitudesProveedor();

        call.enqueue(new Callback<SolicitudesProveedorResponse>() {
            @Override
            public void onResponse(Call<SolicitudesProveedorResponse> call, Response<SolicitudesProveedorResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<SolicitudesProveedorRequest> lista = response.body().getData();
                    if (lista != null && !lista.isEmpty()) {
                        adapter = new SolicitudesProveedorAdapter(
                                getContext(),
                                lista,
                                solicitud -> aprobarSolicitud(solicitud.getId_user())
                        );
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(getContext(), "No hay solicitudes pendientes", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al obtener solicitudes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SolicitudesProveedorResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error al cargar solicitudes", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void aprobarSolicitud(int idUser) {
        new AlertDialog.Builder(getContext())
                .setTitle("¿Aprobar solicitud?")
                .setMessage("¿Estás seguro de que deseas aprobar a este usuario como proveedor?")
                .setPositiveButton("Sí, aprobar", (dialog, which) -> {
                    AprobarProveedorRequest request = new AprobarProveedorRequest(idUser);
                    Call<SolicitudResponse> call = authService.aprobarProveedor(request);

                    call.enqueue(new Callback<SolicitudResponse>() {
                        @Override
                        public void onResponse(Call<SolicitudResponse> call, Response<SolicitudResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(getContext(), "Proveedor aprobado", Toast.LENGTH_SHORT).show();
                                cargarSolicitudes(); // recarga la lista
                            } else {
                                Toast.makeText(getContext(), "Error: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<SolicitudResponse> call, Throwable t) {
                            Toast.makeText(getContext(), "Error al aprobar", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


}
