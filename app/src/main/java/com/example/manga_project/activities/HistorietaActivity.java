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
import com.example.manga_project.Modelos.ComentariosResponse;
import com.example.manga_project.Modelos.CrearComentarioRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

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
    private FloatingActionButton fabAddToCart;
    private boolean volumenEnCarrito = false;
    private boolean enWishlist = false;

    private ArrayList<Comentario> listaComentarios = new ArrayList<>();
    private LinearLayout commentInputContainer;
    private EditText etComment;
    private ImageButton btnSendComment;
    private TabLayout tabLayout;
    private ImageButton btnWishlist;

    private String tipoVolumen = null;
    private boolean lockedCapitulos = false;
    private boolean volumenComprado = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historieta_informacion);

        idVolumen = getIntent().getIntExtra("ID_VOLUMEN", 0);
        if (idVolumen == 0) {
            Toast.makeText(this, "ID de volumen no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ivCover = findViewById(R.id.ivCover);
        tvTitle = findViewById(R.id.tvTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvSynopsis = findViewById(R.id.tvSynopsis);
        rvChapters = findViewById(R.id.rvChapters);
        rvComments = findViewById(R.id.rvComments);
        episodioAdapter = new EpisodioAdapter(new ArrayList<>(), this::cargarPaginasCapitulo);
        rvChapters.setLayoutManager(new LinearLayoutManager(this));
        rvChapters.setAdapter(episodioAdapter);
        comentarioAdapter = new ComentarioAdapter(listaComentarios);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(comentarioAdapter);

        api = ApiClient.getClientConToken().create(AuthService.class);
        cargarComentarios();

        commentInputContainer = findViewById(R.id.commentInputContainer);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        tabLayout = findViewById(R.id.tabLayout);

        fabAddToCart = findViewById(R.id.fabAddToCart);
        fabAddToCart.setOnClickListener(v -> {
            if (volumenComprado) {
                Toast.makeText(this, "Ya compraste este volumen", Toast.LENGTH_SHORT).show();
                fabAddToCart.setEnabled(false);
                fabAddToCart.setVisibility(View.GONE);
                return;
            }
            if (volumenEnCarrito) {
                Toast.makeText(this, "Ya está en tu carrito", Toast.LENGTH_SHORT).show();
                fabAddToCart.setEnabled(false);
                return;
            }
            agregarAlCarrito();
        });

        btnWishlist = findViewById(R.id.btnWishlist);
        btnWishlist.setOnClickListener(v -> {
            if (!enWishlist) agregarAWishlist(); else eliminarDeWishlist();
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    rvChapters.setVisibility(View.GONE);
                    rvComments.setVisibility(View.VISIBLE);
                    commentInputContainer.setVisibility(View.VISIBLE);
                    fabAddToCart.setVisibility(View.GONE); // Ocultar botón flotante en comentarios
                } else {
                    rvChapters.setVisibility(View.VISIBLE);
                    rvComments.setVisibility(View.GONE);
                    commentInputContainer.setVisibility(View.GONE);
                    if (!volumenComprado) {
                        fabAddToCart.setVisibility(View.VISIBLE); // Mostrar solo si no está comprado
                    }
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnSendComment.setOnClickListener(v -> {
            String texto = etComment.getText().toString().trim();
            if (!texto.isEmpty()) {
                api.crearComentario(new CrearComentarioRequest(idVolumen, texto))
                   .enqueue(new Callback<CrearComentarioResponse>() {
                    @Override public void onResponse(Call<CrearComentarioResponse> call, Response<CrearComentarioResponse> response) {
                        if (response.isSuccessful() && response.body()!=null) {
                            cargarComentarios();
                            etComment.setText("");
                        } else {
                            Toast.makeText(HistorietaActivity.this, "No se pudo publicar el comentario", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<CrearComentarioResponse> call, Throwable t) {
                        Toast.makeText(HistorietaActivity.this, "Error de red al comentar", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Escribe un comentario", Toast.LENGTH_SHORT).show();
            }
        });

        verificarVolumenEnCarrito();
        verificarVolumenComprado();
        cargarWishlist();
        cargarFicha();
        cargarCapitulos();
    }

    private void updateWishlistIcon(boolean filled, String desc) {
        btnWishlist.setImageResource(filled ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp);
        btnWishlist.setContentDescription(desc);
    }

    private void verificarVolumenEnCarrito() {
        api.listarCarrito().enqueue(new Callback<ListarCarritoResponse>() {
            @Override public void onResponse(Call<ListarCarritoResponse> call, Response<ListarCarritoResponse> response) {
                if (response.isSuccessful() && response.body()!=null && response.body().items!=null) {
                    for (ListarCarritoResponse.ItemCarrito item: response.body().items) {
                        if (item.id_volumen==idVolumen) {
                            volumenEnCarrito=true;
                            updateWishlistIcon(true, "En tu biblioteca");
                            fabAddToCart.setEnabled(false);
                            fabAddToCart.setImageResource(R.drawable.ic_check);
                            break;
                        }
                    }
                }
            }
            @Override public void onFailure(Call<ListarCarritoResponse> call, Throwable t) {}
        });
    }

    private void verificarVolumenComprado() {
        api.getItemsUsuario("purchases").enqueue(new Callback<com.example.manga_project.Modelos.ItemsUsuarioResponse>() {
            @Override
            public void onResponse(Call<com.example.manga_project.Modelos.ItemsUsuarioResponse> call, Response<com.example.manga_project.Modelos.ItemsUsuarioResponse> response) {
                if (response.isSuccessful() && response.body()!=null && response.body().data!=null) {
                    for (com.example.manga_project.Modelos.ItemUsuario item: response.body().data) {
                        if (item.id_volumen==idVolumen) {
                            // VERIFICAR SI FUE DEVUELTO - solo bloquear si NO fue devuelto exitosamente
                            if (!"succeeded".equals(item.estadoDevolucion)) {
                                android.util.Log.d("HISTOR", "Volumen " + idVolumen + " ya comprado, estado devolución: " + item.estadoDevolucion);
                                volumenComprado = true;
                                fabAddToCart.setEnabled(false);
                                fabAddToCart.setImageResource(R.drawable.ic_check);
                                fabAddToCart.setVisibility(View.GONE); // Ocultar si ya está comprado
                            } else {
                                android.util.Log.d("HISTOR", "Volumen " + idVolumen + " fue devuelto exitosamente, permitir recompra");
                                volumenComprado = false; // Permitir recompra después de devolución exitosa
                                fabAddToCart.setVisibility(View.VISIBLE);
                                fabAddToCart.setEnabled(true);
                            }
                            break;
                        }
                    }
                } else {
                    android.util.Log.e("HISTOR", "Error al verificar compras: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<com.example.manga_project.Modelos.ItemsUsuarioResponse> call, Throwable t) {
                android.util.Log.e("HISTOR", "Error de red al verificar compras", t);
            }
        });
    }

    private void cargarWishlist() {
        api.getItemsUsuario("wishlist").enqueue(new Callback<com.example.manga_project.Modelos.ItemsUsuarioResponse>() {
            @Override public void onResponse(Call<com.example.manga_project.Modelos.ItemsUsuarioResponse> call, Response<com.example.manga_project.Modelos.ItemsUsuarioResponse> response) {
                if (response.isSuccessful() && response.body()!=null && response.body().data!=null) {
                    for (com.example.manga_project.Modelos.ItemUsuario item: response.body().data) {
                        if (item.id_volumen==idVolumen) {
                            enWishlist=true;
                            updateWishlistIcon(true, "En tu lista de deseos");
                            return;
                        }
                    }
                }
                updateWishlistIcon(false, "Agregar a lista de deseos");
            }
            @Override public void onFailure(Call<com.example.manga_project.Modelos.ItemsUsuarioResponse> call, Throwable t) {
                updateWishlistIcon(false, "Agregar a lista de deseos");
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
                    if (ficha.portada != null && !ficha.portada.trim().isEmpty()) {
                        Picasso.get().load(ficha.portada).placeholder(R.drawable.ic_placeholder_portada).into(ivCover);
                    } else {
                        ivCover.setImageResource(R.drawable.ic_placeholder_portada);
                    }
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
                    tipoVolumen = response.body().tipo;
                    lockedCapitulos = response.body().locked;
                    episodioAdapter.actualizarDatos(response.body().chapters);
                    // Si es comic, nunca mostrar mensaje de compra (locked)
                    if ("comic".equalsIgnoreCase(tipoVolumen)) {
                        episodioAdapter.setLocked(false);
                    } else {
                        episodioAdapter.setLocked(lockedCapitulos);
                    }
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
                    // Si es comic y está locked, solo mostrar 5 páginas
                    if ("comic".equalsIgnoreCase(tipoVolumen) && lockedCapitulos) {
                        if (urls.size() > 5) {
                            urls = new ArrayList<>(urls.subList(0, 5));
                            Toast.makeText(HistorietaActivity.this, "Solo puedes leer 5 páginas gratis. Compra para desbloquear el cómic completo.", Toast.LENGTH_LONG).show();
                        }
                    }
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

                    // Verificar si ya compró (pero sin devolución)
                    if (msg != null && msg.toLowerCase().contains("ya compraste")) {
                        Toast.makeText(HistorietaActivity.this, msg, Toast.LENGTH_LONG).show();
                        fabAddToCart.setEnabled(false);
                        fabAddToCart.setVisibility(View.GONE);
                        return;
                    }

                    if (code == 0) {
                        Toast.makeText(HistorietaActivity.this, "¡Volumen añadido a tu carrito!", Toast.LENGTH_SHORT).show();
                    } else if (code == 2) {
                        Toast.makeText(HistorietaActivity.this, "Este volumen ya está en tu carrito.", Toast.LENGTH_LONG).show();
                    } else if (code == 1) {
                        Toast.makeText(HistorietaActivity.this, "La cantidad debe ser al menos 1.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(HistorietaActivity.this, msg != null ? msg : "No se pudo agregar al carrito.", Toast.LENGTH_LONG).show();
                    }
                } else if (response.errorBody() != null) {
                    try {
                        String errorJson = response.errorBody().string();
                        android.util.Log.d("CARRITO", "Error response: " + errorJson);

                        // Verificar específicamente si es un problema de compra previa
                        if (errorJson.toLowerCase().contains("ya compraste")) {
                            // Solo bloquear si realmente no puede comprar de nuevo
                            // (el backend debería permitir recompras después de devoluciones)
                            Toast.makeText(HistorietaActivity.this, "Ya tienes una compra activa de este volumen", Toast.LENGTH_LONG).show();
                            fabAddToCart.setEnabled(false);
                            fabAddToCart.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(HistorietaActivity.this, "No se pudo agregar al carrito.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("CARRITO", "Error parsing error body", e);
                        Toast.makeText(HistorietaActivity.this, "No se pudo agregar al carrito.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(HistorietaActivity.this, "No se pudo agregar al carrito. Intenta más tarde.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaGenerica> call, Throwable t) {
                android.util.Log.e("CARRITO", "Network error al agregar al carrito", t);
                Toast.makeText(HistorietaActivity.this, "Error de conexión. Revisa tu internet.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cargarComentarios() {
        Call<ComentariosResponse> call = api.getComentarios(idVolumen);
        call.enqueue(new Callback<ComentariosResponse>() {
            @Override
            public void onResponse(Call<ComentariosResponse> call, Response<ComentariosResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    comentarioAdapter.setComentarios(response.body().comentarios);
                } else {
                    comentarioAdapter.setComentarios(new ArrayList<>());
                }
            }
            @Override
            public void onFailure(Call<ComentariosResponse> call, Throwable t) {
                comentarioAdapter.setComentarios(new ArrayList<>());
            }
        });
    }

    private void agregarAWishlist() {
        // block wishlist if already purchased
        if (volumenEnCarrito) {
            Toast.makeText(this, "Ya compraste esta historieta", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Integer> body = new java.util.HashMap<>();
        body.put("id_volumen", idVolumen);
        api.agregarWishlist(body).enqueue(new Callback<RespuestaGenerica>() {
            @Override
            public void onResponse(Call<RespuestaGenerica> call, Response<RespuestaGenerica> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String msg = response.body().msg != null ? response.body().msg : "Agregado a la lista de deseos";
                    Toast.makeText(HistorietaActivity.this, msg, Toast.LENGTH_SHORT).show();
                    ImageButton btnWishlist = findViewById(R.id.btnWishlist);
                    btnWishlist.setImageResource(R.drawable.ic_favorite_white_24dp);
                    btnWishlist.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150)
                        .withEndAction(() -> btnWishlist.animate().scaleX(1f).scaleY(1f).setDuration(100));
                    enWishlist = true;
                } else {
                    Toast.makeText(HistorietaActivity.this, "Error al agregar a la lista de deseos", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<RespuestaGenerica> call, Throwable t) {
                Toast.makeText(HistorietaActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void eliminarDeWishlist() {
        // block wishlist removal if purchased
        if (volumenEnCarrito) {
            Toast.makeText(this, "Ya compraste esta historieta", Toast.LENGTH_SHORT).show();
            return;
        }
        api.eliminarWishlist(idVolumen).enqueue(new Callback<RespuestaGenerica>() {
            @Override
            public void onResponse(Call<RespuestaGenerica> call, Response<RespuestaGenerica> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String msg = response.body().msg != null ? response.body().msg : "Eliminado de la lista de deseos";
                    Toast.makeText(HistorietaActivity.this, msg, Toast.LENGTH_SHORT).show();
                    ImageButton btnWishlist = findViewById(R.id.btnWishlist);
                    btnWishlist.setImageResource(R.drawable.ic_favorite_border_white_24dp);
                    btnWishlist.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150)
                        .withEndAction(() -> btnWishlist.animate().scaleX(1f).scaleY(1f).setDuration(100));
                    enWishlist = false;
                } else {
                    Toast.makeText(HistorietaActivity.this, "Error al quitar de la lista de deseos", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<RespuestaGenerica> call, Throwable t) {
                Toast.makeText(HistorietaActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
