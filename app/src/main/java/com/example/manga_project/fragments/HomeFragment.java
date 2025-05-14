package com.example.manga_project.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.example.manga_project.Api_cliente.ApiResponse;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Api_cliente.AutoresApiResponse;
import com.example.manga_project.Api_cliente.Libro;
import com.example.manga_project.R;
import com.example.manga_project.Section;
import com.example.manga_project.adapters.SectionAdapter;
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

        // Configura Google Sign-In para cerrar sesión
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Configura Retrofit y AuthService
        // Asegúrate de que el contexto esté configurado en ApiClient
        ApiClient.setContext(requireContext());  // Establecer el contexto de la aplicación

        // Configura Retrofit y AuthService usando el cliente con token
        Retrofit retrofit = ApiClient.getClientConToken();  // Obtener el cliente Retrofit con el token
        authService = retrofit.create(AuthService.class);

        // Configurar el RecyclerView principal con disposición vertical
        RecyclerView recyclerView = binding.mRvHome;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));


        // Llamada a las diferentes APIs
        cargarSecciones(recyclerView);
    }

    private void cargarSecciones(RecyclerView recyclerView) {
        List<Section> sections = new ArrayList<>();

        // Cargar cada sección llamando a las diferentes APIs
        cargarLibrosMasVendidos(sections, recyclerView);
        cargarMejoresAutoresLibros(sections, recyclerView);
        cargarLibrosAntiguos(sections, recyclerView);
        cargarLibrosActuales(sections, recyclerView);
    }

    private void cargarLibrosMasVendidos(List<Section> sections, RecyclerView recyclerView) {
        Call<ApiResponse> call = authService.obtenerLibrosMasVendidos();
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Libro> libros = response.body().getLibros();
                    sections.add(new Section("Libros más vendidos", libros));
                    actualizarAdaptador(recyclerView, sections);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API Response", "Error: " + t.getMessage());
            }
        });
    }

    private void cargarMejoresAutoresLibros(List<Section> sections, RecyclerView recyclerView) {
        Call<AutoresApiResponse> call = authService.obtenerMejoresAutoresLibros();
        call.enqueue(new Callback<AutoresApiResponse>() {
            @Override
            public void onResponse(Call<AutoresApiResponse> call, Response<AutoresApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Libro> libros = response.body().getLibros();
                    sections.add(new Section("Mejores escritores", libros));
                    actualizarAdaptador(recyclerView, sections);
                }
            }

            @Override
            public void onFailure(Call<AutoresApiResponse> call, Throwable t) {
                Log.e("API Response", "Error: " + t.getMessage());
            }
        });
    }


    private void cargarLibrosAntiguos(List<Section> sections, RecyclerView recyclerView) {
        Call<ApiResponse> call = authService.obtenerLibrosAntiguos();
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Libro> libros = response.body().getLibros();
                    sections.add(new Section("Libros antiguos", libros));
                    actualizarAdaptador(recyclerView, sections);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API Response", "Error: " + t.getMessage());
            }
        });
    }

    private void cargarLibrosActuales(List<Section> sections, RecyclerView recyclerView) {
        Call<ApiResponse> call = authService.obtenerLibrosActuales();
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Libro> libros = response.body().getLibros();
                    sections.add(new Section("Libros actuales", libros));
                    actualizarAdaptador(recyclerView, sections);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API Response", "Error: " + t.getMessage());
            }
        });
    }

    private void actualizarAdaptador(RecyclerView recyclerView, List<Section> sections) {
        // Crea el adaptador solo con la lista de secciones y el contexto
        SectionAdapter adapter = new SectionAdapter(sections, requireContext());
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