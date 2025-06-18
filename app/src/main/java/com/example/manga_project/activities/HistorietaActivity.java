package com.example.manga_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.CarritoRequest;
import com.example.manga_project.Modelos.FichaVolumenResponse;
import com.example.manga_project.Modelos.CapituloResponse;
import com.example.manga_project.Modelos.PaginaResponse;
import com.example.manga_project.Modelos.RespuestaGenerica;
import com.example.manga_project.R;
import com.example.manga_project.adapters.EpisodioAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorietaActivity extends AppCompatActivity {
    private int idVolumen;
    private AuthService api;
    private ImageView ivCover;
    private TextView tvTitle, tvPrice, tvSynopsis;
    private RecyclerView rvChapters;
    private EpisodioAdapter episodioAdapter;
    private FloatingActionButton fabRead;
    private FloatingActionButton fabAddToCart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historieta_informacion);

        // Obtener id_volumen del intent
        idVolumen = getIntent().getIntExtra("ID_VOLUMEN", 0);
        if (idVolumen == 0) {
            Toast.makeText(this, "ID de volumen no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar views
        ivCover = findViewById(R.id.ivCover);
        tvTitle = findViewById(R.id.tvTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvSynopsis = findViewById(R.id.tvSynopsis);
        rvChapters = findViewById(R.id.rvChapters);
        episodioAdapter = new EpisodioAdapter(new ArrayList<>(), episodeId -> cargarPaginasCapitulo(episodeId));
        rvChapters.setLayoutManager(new LinearLayoutManager(this));
        rvChapters.setAdapter(episodioAdapter);

        // Inicializar API
        api = ApiClient.getClientConToken().create(AuthService.class);

        // Inicializar botones flotantes

        fabAddToCart = findViewById(R.id.fabAddToCart);
        fabAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarAlCarrito();
            }
        });

        cargarFicha();
        cargarCapitulos();
    }

    private void cargarFicha() {
        api.getFichaVolumen(idVolumen).enqueue(new Callback<FichaVolumenResponse>() {
            @Override
            public void onResponse(Call<FichaVolumenResponse> call, Response<FichaVolumenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FichaVolumenResponse ficha = response.body();
                    tvTitle.setText(ficha.titulo);
                    tvPrice.setText("S/" + ficha.precio);
                    tvSynopsis.setText(ficha.sinopsis);
                    Picasso.get().load(ficha.portada).placeholder(R.drawable.ic_placeholder_portada).into(ivCover);
                }
            }
            @Override
            public void onFailure(Call<FichaVolumenResponse> call, Throwable t) {
                Toast.makeText(HistorietaActivity.this, "Error al cargar ficha", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarCapitulos() {
        api.getCapitulosVolumen(idVolumen).enqueue(new Callback<CapituloResponse>() {
            @Override
            public void onResponse(Call<CapituloResponse> call, Response<CapituloResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().code == 0) {
                    episodioAdapter.actualizarDatos(response.body().chapters);
                }
            }
            @Override
            public void onFailure(Call<CapituloResponse> call, Throwable t) {
                Toast.makeText(HistorietaActivity.this, "Error al cargar capítulos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarPaginasCapitulo(String chapter) {
        api.getPaginasCapitulo(idVolumen, chapter).enqueue(new Callback<PaginaResponse>() {
            @Override
            public void onResponse(Call<PaginaResponse> call, Response<PaginaResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().code == 0) {
                    ArrayList<String> urls = new ArrayList<>(response.body().pages);
                    Intent i = new Intent(HistorietaActivity.this, HistorietaReaderActivity.class);
                    i.putStringArrayListExtra("PAGES", urls);
                    startActivity(i);
                } else {
                    Toast.makeText(HistorietaActivity.this, "No se pudieron cargar las páginas", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<PaginaResponse> call, Throwable t) {
                Toast.makeText(HistorietaActivity.this, "Error de red al cargar páginas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agregarAlCarrito() {
        // Crear request para agregar al carrito
        CarritoRequest req = new CarritoRequest(idVolumen, 1);
        api.agregarAlCarrito(req).enqueue(new Callback<RespuestaGenerica>() {
            @Override
            public void onResponse(Call<RespuestaGenerica> call, Response<RespuestaGenerica> response) {
                if (response.isSuccessful() && response.body() != null && response.body().code == 0) {
                    Toast.makeText(HistorietaActivity.this, "Añadido al carrito", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HistorietaActivity.this, "No se pudo agregar al carrito", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<RespuestaGenerica> call, Throwable t) {
                Toast.makeText(HistorietaActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

