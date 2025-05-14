package com.example.manga_project.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.ApiResponse;
import com.example.manga_project.Api_cliente.Libro;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.adapters.SearchBookAdapter;
import com.example.manga_project.databinding.FragmentSearchBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchBookAdapter searchBookAdapter;
    private List<Libro> todosLosLibros = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configura el RecyclerView
        RecyclerView recyclerView = binding.recyclerViewBooks;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        searchBookAdapter = new SearchBookAdapter(new ArrayList<>(), requireContext());
        recyclerView.setAdapter(searchBookAdapter);

        // Configura el campo de búsqueda con un OnQueryTextListener
        TextInputEditText searchEditText = (TextInputEditText) binding.searchTextInputLayout.getEditText();
        if (searchEditText != null) {
            searchEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    filtrarLibros(searchEditText.getText().toString());
                    return true;
                }
                return false;
            });

            // También agrega un listener para filtrar en tiempo real mientras se escribe
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filtrarLibros(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }

        // Llama a la API
        obtenerLibrosDesdeApi();
    }

    private void obtenerLibrosDesdeApi() {
        AuthService apiService = ApiClient.getClientConToken().create(AuthService.class);
        Call<ApiResponse> call = apiService.obtenerLibros();

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    todosLosLibros = response.body().getLibros();
                    searchBookAdapter.setBooks(todosLosLibros);
                } else {
                    Toast.makeText(requireContext(), "Error al obtener libros", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filtrarLibros(String texto) {
        List<Libro> librosFiltrados = new ArrayList<>();
        for (Libro libro : todosLosLibros) {
            if (libro.getTitulo_lib().toLowerCase().contains(texto.toLowerCase())) {
                librosFiltrados.add(libro);
            }
        }
        searchBookAdapter.setBooks(librosFiltrados);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}