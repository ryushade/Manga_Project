package com.example.manga_project.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.BusquedaHistorieta;
import com.example.manga_project.Modelos.BusquedaHistorietaResponse;
import com.example.manga_project.R;
import com.example.manga_project.activities.HistorietaActivity;
import com.google.android.material.textfield.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SearchFragment extends Fragment {
    private RecyclerView recyclerView;
    private SearchResultAdapter adapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY = 400; // ms

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_books);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchResultAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        // Listener para abrir historieta al hacer clic
        adapter.setOnItemClickListener(historieta -> {
            if (historieta != null && historieta.id_historieta > 0) {
                Intent intent = new Intent(getContext(), HistorietaActivity.class);
                intent.putExtra("ID_VOLUMEN", historieta.id_historieta);
                startActivity(intent);
            }
        });

        TextInputEditText searchEditText = view.findViewById(R.id.search_edit_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buscarHistorietas(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void buscarHistorietas(String query) {
        android.util.Log.d("SearchFragment", "Llamando a la API de búsqueda con query: " + query);
        if (query.isEmpty()) {
            adapter.setResultados(new ArrayList<>());
            Toast.makeText(getContext(), "Ingresa un término para buscar", Toast.LENGTH_SHORT).show();
            return;
        }
        Retrofit retrofit = ApiClient.getClientConToken();
        AuthService apiService = retrofit.create(AuthService.class);
        apiService.buscarHistorietas(query).enqueue(new Callback<BusquedaHistorietaResponse>() {
            @Override
            public void onResponse(Call<BusquedaHistorietaResponse> call, Response<BusquedaHistorietaResponse> response) {
                android.util.Log.d("SearchFragment", "Respuesta de la API: " + response.code() + ", body: " + response.body());
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<BusquedaHistorieta> resultados = response.body().data;
                    if (resultados != null && !resultados.isEmpty()) {
                        adapter.setResultados(resultados);
                    } else {
                        adapter.setResultados(new ArrayList<>());
                        Toast.makeText(getContext(), "No se encontraron resultados", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    adapter.setResultados(new ArrayList<>());
                    Toast.makeText(getContext(), "Error al buscar. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BusquedaHistorietaResponse> call, Throwable t) {
                android.util.Log.e("SearchFragment", "Error al llamar a la API de búsqueda", t);
                adapter.setResultados(new ArrayList<>());
                Toast.makeText(getContext(), "Error de red. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
