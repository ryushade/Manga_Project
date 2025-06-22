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
        binding.btnRead.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Read pulsado", Toast.LENGTH_SHORT).show()
        );
        binding.btnMyList.setOnClickListener(v ->
                Toast.makeText(requireContext(), "My List pulsado", Toast.LENGTH_SHORT).show()
        );
    }

    private void cargarSeccionesVolumen() {
        // Novedades
        authService.getNovedades().enqueue(new Callback<List<VolumenResponse>>() {
            @Override
            public void onResponse(Call<List<VolumenResponse>> call,
                                   Response<List<VolumenResponse>> resp) {
                if (!isAdded() || binding == null) return;
                if (resp.isSuccessful() && resp.body() != null) {
                    sections.add(new SectionVolumen("Novedades", resp.body()));
                    actualizarAdaptador();
                }
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

        // Por género
        authService.getPorGenero(1).enqueue(new Callback<List<VolumenResponse>>() {
            @Override
            public void onResponse(Call<List<VolumenResponse>> call,
                                   Response<List<VolumenResponse>> resp) {
                if (!isAdded() || binding == null) return;
                if (resp.isSuccessful() && resp.body() != null) {
                    sections.add(new SectionVolumen("Por género", resp.body()));
                    actualizarAdaptador();
                }
            }
            @Override public void onFailure(Call<List<VolumenResponse>> call, Throwable t) { }
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
