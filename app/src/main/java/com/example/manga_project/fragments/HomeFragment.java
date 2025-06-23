package com.example.manga_project.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.VolumenResponse;
import com.example.manga_project.R;
import com.example.manga_project.SectionVolumen;
import com.example.manga_project.activities.LoginActivity;
import com.example.manga_project.adapters.HomeAdapter;
import com.example.manga_project.databinding.FragmentHomeBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private AuthService authService;
    // Reutilizamos la misma lista para ir agregando secciones
    private final List<SectionVolumen> sections = new ArrayList<>();
    private VolumenResponse ultimaHistorieta = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 1) Inflamos el binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 2) Preparamos Retrofit con token
        Retrofit retrofit = ApiClient.getClientConToken();
        authService = retrofit.create(AuthService.class);

        // 3) Configuramos el RecyclerView
        binding.rvHome.setHasFixedSize(true);
        binding.rvHome.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 4) Disparamos las tres peticiones
        cargarSeccionesVolumen();

        // 5) Botones de prueba
        binding.btnRead.setText("Ver detalle");
        binding.btnMyList.setText("Guardar");
        binding.btnRead.setOnClickListener(v -> {
            if (ultimaHistorieta != null) {
                Intent intent = new Intent(requireContext(), com.example.manga_project.activities.HistorietaActivity.class);
                intent.putExtra("ID_VOLUMEN", ultimaHistorieta.id_volumen);
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "No hay historieta para mostrar", Toast.LENGTH_SHORT).show();
            }
        });
        binding.btnMyList.setOnClickListener(v -> {
            if (ultimaHistorieta != null) {
                // Llamar a la API para agregar a la lista de deseos
                java.util.Map<String, Integer> body = new java.util.HashMap<>();
                body.put("id_volumen", ultimaHistorieta.id_volumen);
                authService.agregarWishlist(body).enqueue(new retrofit2.Callback<com.example.manga_project.Modelos.RespuestaGenerica>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.manga_project.Modelos.RespuestaGenerica> call, retrofit2.Response<com.example.manga_project.Modelos.RespuestaGenerica> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().code == 0) {
                            Toast.makeText(requireContext(), "¡Añadido a la lista de deseos!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "No se pudo guardar", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(retrofit2.Call<com.example.manga_project.Modelos.RespuestaGenerica> call, Throwable t) {
                        Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(requireContext(), "No hay historieta para guardar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarSeccionesVolumen() {
        // Limpiar secciones antes de agregar nuevas
        sections.clear();
        ultimaHistorieta = null;
        // Novedades
        authService.getNovedades().enqueue(new Callback<List<VolumenResponse>>() {
            @Override
            public void onResponse(Call<List<VolumenResponse>> call,
                                   Response<List<VolumenResponse>> resp) {
                if (!isAdded() || binding == null) return;
                if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                    // Guardar la última historieta para el header
                    ultimaHistorieta = resp.body().get(resp.body().size() - 1);
                    if (ultimaHistorieta.portada != null && !ultimaHistorieta.portada.isEmpty()) {
                        Picasso.get().load(ultimaHistorieta.portada)
                                .placeholder(R.drawable.header_background)
                                .into(binding.imgHeaderCover);
                    }
                    // Agregar sección de novedades con TODAS las historietas (incluida la última)
                    sections.add(new SectionVolumen("Novedades", resp.body()));
                }
                actualizarAdaptador();
            }
            @Override public void onFailure(Call<List<VolumenResponse>> call, Throwable t) { }
        });

        // Más vendidas
        authService.getMasVendidas().enqueue(new Callback<List<VolumenResponse>>() {
            @Override
            public void onResponse(Call<List<VolumenResponse>> call,
                                   Response<List<VolumenResponse>> resp) {
                if (!isAdded() || binding == null) return;
                if (resp.isSuccessful() && resp.body() != null) {
                    sections.add(new SectionVolumen("Más vendidas", resp.body()));
                    actualizarAdaptador();
                }
            }
            @Override public void onFailure(Call<List<VolumenResponse>> call, Throwable t) { }
        });

        // Más vendidos (nuevo endpoint y modelo)
        authService.getMasVendidos().enqueue(new Callback<List<com.example.manga_project.Modelos.MasVendidoResponse>>() {
            @Override
            public void onResponse(Call<List<com.example.manga_project.Modelos.MasVendidoResponse>> call,
                                   Response<List<com.example.manga_project.Modelos.MasVendidoResponse>> resp) {
                if (!isAdded() || binding == null) return;
                if (resp.isSuccessful() && resp.body() != null) {
                    // Adaptar la lista de MasVendidoResponse a VolumenResponse para el adaptador
                    List<com.example.manga_project.Modelos.VolumenResponse> lista = new ArrayList<>();
                    for (com.example.manga_project.Modelos.MasVendidoResponse mv : resp.body()) {
                        com.example.manga_project.Modelos.VolumenResponse v = new com.example.manga_project.Modelos.VolumenResponse();
                        v.id_volumen = mv.id_volumen;
                        v.titulo = mv.titulo;
                        v.portada = mv.portada_url;
                        // Puedes agregar un campo extra si tu adaptador lo soporta
                        lista.add(v);
                    }
                    sections.add(new SectionVolumen("Más vendidos", lista));
                    actualizarAdaptador();
                }
            }
            @Override public void onFailure(Call<List<com.example.manga_project.Modelos.MasVendidoResponse>> call, Throwable t) { }
        });
    }

    private void actualizarAdaptador() {
        if (binding == null) return;
        HomeAdapter adapter = new HomeAdapter(requireContext(), sections);
        binding.rvHome.setAdapter(adapter);
    }

    private void logout() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        startActivity(new Intent(requireActivity(), LoginActivity.class));
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 6) Liberamos el binding para evitar fugas y NPE posteriores
        binding = null;
    }
}
