package com.example.manga_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manga_project.Modelos.Solicitud;
import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;

import com.example.manga_project.R;
import com.example.manga_project.adapters.SolicitudAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SolicitudesPublicacionFragment extends Fragment {

    private RecyclerView recyclerView;
    private SolicitudAdapter adapter;
    private List<Solicitud> listaSolicitudes;
    private AuthService api;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_solicitud_historieta_proveedor, container, false);

        recyclerView = view.findViewById(R.id.recyclerSolicitudesPublicacion);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar lista vacía
        listaSolicitudes = new ArrayList<>();

        // Inicializar el adaptador
        adapter = new SolicitudAdapter(listaSolicitudes);
        recyclerView.setAdapter(adapter);

        // Inicializar Retrofit
        api = ApiClient.getClientConToken().create(AuthService.class);

        // Llamada al backend para obtener solicitudes
        obtenerSolicitudes();

        return view;
    }

    private void obtenerSolicitudes() {
        api.obtenerSolicitudesPublicacion().enqueue(new Callback<List<Solicitud>>() {
            @Override
            public void onResponse(Call<List<Solicitud>> call, Response<List<Solicitud>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaSolicitudes.clear();
                    listaSolicitudes.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Error al cargar solicitudes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Solicitud>> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
