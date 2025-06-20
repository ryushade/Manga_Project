package com.example.manga_project.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.VolumenResponse;
import com.example.manga_project.R;
import com.example.manga_project.SectionVolumen;
import com.example.manga_project.adapters.SectionVolumenAdapter;
import com.example.manga_project.activities.LoginActivity;
import com.example.manga_project.databinding.FragmentHomeBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HomeFragment extends Fragment {
    private GoogleSignInClient mGoogleSignInClient;
    private FragmentHomeBinding binding;
    private AuthService authService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        ApiClient.setContext(requireContext());
        Retrofit retrofit = ApiClient.getClientConToken();
        authService = retrofit.create(AuthService.class);
        RecyclerView recyclerView = binding.mRvHome;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        cargarSeccionesVolumen(recyclerView);
    }

    private void cargarSeccionesVolumen(RecyclerView recyclerView) {
        List<SectionVolumen> sections = new ArrayList<>();
        // Novedades
        authService.getNovedades().enqueue(new Callback<List<VolumenResponse>>() {
            @Override
            public void onResponse(Call<List<VolumenResponse>> call, Response<List<VolumenResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sections.add(new SectionVolumen("Novedades", response.body()));
                    actualizarAdaptadorVolumen(recyclerView, sections);
                }
            }

            @Override public void onFailure(Call<List<VolumenResponse>> call, Throwable t) {}
        });
        // Más vendidas
        authService.getMasVendidas().enqueue(new Callback<List<VolumenResponse>>() {
            @Override
            public void onResponse(Call<List<VolumenResponse>> call, Response<List<VolumenResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sections.add(new SectionVolumen("Más vendidas", response.body()));
                    actualizarAdaptadorVolumen(recyclerView, sections);
                }
            }

            @Override public void onFailure(Call<List<VolumenResponse>> call, Throwable t) {}
        });
        // Por género (ejemplo: género 1)
        authService.getPorGenero(1).enqueue(new Callback<List<VolumenResponse>>() {
            @Override
            public void onResponse(Call<List<VolumenResponse>> call, Response<List<VolumenResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sections.add(new SectionVolumen("Por género", response.body()));
                    actualizarAdaptadorVolumen(recyclerView, sections);
                }
            }

            @Override public void onFailure(Call<List<VolumenResponse>> call, Throwable t) {}
        });
    }

    private void actualizarAdaptadorVolumen(RecyclerView recyclerView, List<SectionVolumen> sections) {
        SectionVolumenAdapter adapter = new SectionVolumenAdapter(sections, requireContext());
        recyclerView.setAdapter(adapter);
    }



    // Método para mostrar el PopupMenu
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.app_bar_menu, popupMenu.getMenu());

        // Manejar los clics en las opciones del menú
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                logout();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    // Método para cerrar sesión
    private void logout() {
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
        });
        // Eliminar todos los datos de SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear(); // Borra todos los datos
        editor.apply();

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish(); // Cierra la actividad contenedora si es necesario
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}