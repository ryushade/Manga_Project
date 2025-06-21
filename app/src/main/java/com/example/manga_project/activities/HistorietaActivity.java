package com.example.manga_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.CarritoRequest;
import com.example.manga_project.Modelos.CrearComentarioResponse;
import com.example.manga_project.Modelos.FichaVolumenResponse;
import com.example.manga_project.Modelos.CapituloResponse;
import com.example.manga_project.Modelos.ListarCarritoResponse;
import com.example.manga_project.Modelos.PaginaResponse;
import com.example.manga_project.Modelos.RespuestaGenerica;
import com.example.manga_project.R;
import com.example.manga_project.adapters.EpisodioAdapter;
import com.example.manga_project.adapters.ComentarioAdapter;
import com.example.manga_project.Modelos.Comentario;
import com.example.manga_project.Modelos.CrearComentarioRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorietaActivity extends AppCompatActivity {
    private int idVolumen;
    private AuthService api;
    private ImageView ivCover;
    private TextView tvTitle, tvPrice, tvSynopsis;
    private RecyclerView rvChapters, rvComments;
    private EpisodioAdapter episodioAdapter;
    private ComentarioAdapter comentarioAdapter;
    private FloatingActionButton fabRead;
    private FloatingActionButton fabAddToCart;
    private boolean volumenEnCarrito = false;
    private ArrayList<Comentario> listaComentarios = new ArrayList<>();
    private LinearLayout commentInputContainer;
    private EditText etComment;
    private ImageButton btnSendComment;
    private TabLayout tabLayout;

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
        rvComments = findViewById(R.id.rvComments);
        episodioAdapter = new EpisodioAdapter(new ArrayList<>(), episodeId -> cargarPaginasCapitulo(episodeId));
        rvChapters.setLayoutManager(new LinearLayoutManager(this));
        rvChapters.setAdapter(episodioAdapter);
        comentarioAdapter = new ComentarioAdapter(listaComentarios);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(comentarioAdapter);
        commentInputContainer = findViewById(R.id.commentInputContainer);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        tabLayout = findViewById(R.id.tabLayout);

        // Inicializar API
        api = ApiClient.getClientConToken().create(AuthService.class);

        // Inicializar botones flotantes
        fabAddToCart = findViewById(R.id.fabAddToCart);
        fabAddToCart.setOnClickListener(v -> {
            if (volumenEnCarrito) {
                Toast.makeText(HistorietaActivity.this, "Ya está en tu carrito", Toast.LENGTH_SHORT).show();
            } else {
                agregarAlCarrito();
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) { // Comentarios
                    rvChapters.setVisibility(View.GONE);
                    rvComments.setVisibility(View.VISIBLE);
                    commentInputContainer.setVisibility(View.VISIBLE);
                } else {
                    rvChapters.setVisibility(View.VISIBLE);
                    rvComments.setVisibility(View.GONE);
                    commentInputContainer.setVisibility(View.GONE);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnSendComment.setOnClickListener(v -> {
            String texto = etComment.getText().toString().trim();
            if (!texto.isEmpty()) {
                // Crear el comentario
                CrearComentarioRequest req = new CrearComentarioRequest(idVolumen, texto);
                api.crearComentario(req).enqueue(new Callback<CrearComentarioResponse>() {
                    @Override
                    public void onResponse(Call<CrearComentarioResponse> call, Response<CrearComentarioResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Después de crear, refresca la lista de comentarios
                            cargarComentarios();
                            etComment.setText("");
                        } else {
                            Toast.makeText(HistorietaActivity.this, "No se pudo publicar el comentario", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<CrearComentarioResponse> call, Throwable t) {
                        Toast.makeText(HistorietaActivity.this, "Error de red al comentar", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Escribe un comentario", Toast.LENGTH_SHORT).show();
            }
        });

        // Cargar comentarios al iniciar
        cargarComentarios();

        verificarVolumenEnCarrito();
        cargarFicha();
        cargarCapitulos();
    }

    private void verificarVolumenEnCarrito() {
        api.listarCarrito().enqueue(new Callback<ListarCarritoResponse>() {
            @Override
            public void onResponse(Call<ListarCarritoResponse> call, Response<ListarCarritoResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().items != null) {
                    for (ListarCarritoResponse.ItemCarrito item : response.body().items) {
                        if (item.id_volumen == idVolumen) {
                            volumenEnCarrito = true;
                            fabAddToCart.setEnabled(false);
                            fabAddToCart.setImageResource(R.drawable.ic_check);
                            break;
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<ListarCarritoResponse> call, Throwable t) {
                // No hacer nada especial
            }
        });
    }

    private void cargarFicha() {
        api.getFichaVolumen(idVolumen).enqueue(new Callback<FichaVolumenResponse>() {
            @Override
            public void onResponse(Call<FichaVolumenResponse> call, Response<FichaVolumenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FichaVolumenResponse ficha = response.body();
                    tvTitle.setText(ficha.titulo);
                    tvPrice.setText(getString(R.string.precio_soles, ficha.precio));
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
        CarritoRequest req = new CarritoRequest(idVolumen, 1);
        api.agregarAlCarrito(req).enqueue(new Callback<RespuestaGenerica>() {
            @Override
            public void onResponse(Call<RespuestaGenerica> call, Response<RespuestaGenerica> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int code = response.body().code;
                    String msg = response.body().msg;
                    if (code == 0) {
                        Toast.makeText(HistorietaActivity.this, "¡Volumen añadido a tu carrito!", Toast.LENGTH_SHORT).show();
                    } else if (code == 2) {
                        Toast.makeText(HistorietaActivity.this, "Este volumen ya está en tu carrito.", Toast.LENGTH_LONG).show();
                    } else if (code == 1) {
                        Toast.makeText(HistorietaActivity.this, "La cantidad debe ser al menos 1.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(HistorietaActivity.this, msg != null ? msg : "No se pudo agregar al carrito.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(HistorietaActivity.this, "No se pudo agregar al carrito. Intenta más tarde.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<RespuestaGenerica> call, Throwable t) {
                Toast.makeText(HistorietaActivity.this, "Error de conexión. Revisa tu internet.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cargarComentarios() {
        api.getComentarios(idVolumen).enqueue(new Callback<List<Comentario>>() {
            @Override
            public void onResponse(Call<List<Comentario>> call, Response<List<Comentario>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    comentarioAdapter.setComentarios(response.body());
                } else {
                    comentarioAdapter.setComentarios(new ArrayList<>());
                }
            }
            @Override
            public void onFailure(Call<List<Comentario>> call, Throwable t) {
                comentarioAdapter.setComentarios(new ArrayList<>());
            }
        });
    }
}
