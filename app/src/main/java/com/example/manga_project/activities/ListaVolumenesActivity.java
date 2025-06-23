package com.example.manga_project.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.R;
import com.example.manga_project.adapters.VolumenAdapter;
import com.example.manga_project.Modelos.VolumenResponse;
import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.MasVendidosApiResponse;
import com.example.manga_project.Modelos.MasVendidoResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class ListaVolumenesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private VolumenAdapter adapter;
    private ArrayList<VolumenResponse> listaVolumenes = new ArrayList<>();
    private AuthService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_volumenes);

        recyclerView = findViewById(R.id.recyclerViewVolumenes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VolumenAdapter(listaVolumenes);
        recyclerView.setAdapter(adapter);

        api = ApiClient.getClientConToken().create(AuthService.class);

        String section = getIntent().getStringExtra("section_title");
        setTitle(section);
        cargarVolumenesPorSeccion(section);
    }

    private void cargarVolumenesPorSeccion(String section) {
        if (section != null && section.toLowerCase().contains("novedad")) {
            api.getNovedades().enqueue(new Callback<List<VolumenResponse>>() {
                @Override
                public void onResponse(Call<List<VolumenResponse>> call, Response<List<VolumenResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        listaVolumenes.clear();
                        listaVolumenes.addAll(response.body());
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ListaVolumenesActivity.this, "No se pudieron cargar las novedades", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<List<VolumenResponse>> call, Throwable t) {
                    Toast.makeText(ListaVolumenesActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (section != null && section.toLowerCase().contains("vendido")) {
            api.getMasVendidos().enqueue(new Callback<MasVendidosApiResponse>() {
                @Override
                public void onResponse(Call<MasVendidosApiResponse> call, Response<MasVendidosApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                        listaVolumenes.clear();
                        for (MasVendidoResponse masVendido : response.body().data) {
                            VolumenResponse v = new VolumenResponse();
                            v.id_volumen = masVendido.id_volumen;
                            v.titulo = masVendido.titulo;
                            v.portada = masVendido.portada_url;
                            v.precio = 0; // No hay precio en la respuesta de más vendidos
                            v.anio = "";
                            listaVolumenes.add(v);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ListaVolumenesActivity.this, "No se pudieron cargar los más vendidos", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<MasVendidosApiResponse> call, Throwable t) {
                    Toast.makeText(ListaVolumenesActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Categoría no soportada", Toast.LENGTH_SHORT).show();
        }
    }
}
