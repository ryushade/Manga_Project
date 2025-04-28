package com.example.ebook_proyect.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.ebook_proyect.Api_cliente.ApiClient;
import com.example.ebook_proyect.Api_cliente.ApiResponse;
import com.example.ebook_proyect.Api_cliente.ApiResponse_L;
import com.example.ebook_proyect.Api_cliente.AuthService;
import com.example.ebook_proyect.Api_cliente.Libro;
import com.example.ebook_proyect.R;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DescriptionBookActivity extends AppCompatActivity {

    private MaterialButton backButton; // Declara el botón de retroceso
    private String bookId;
    private TextView textViewTitle, textViewAuthor, textViewDescription, textViewDetails;
    private ImageView imageViewCover;
    private Button buttonAddToCart;
    private Button buttonSave;
    private Libro libroActual; // Variable para almacenar el libro actual
    private Button buttonPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description_book);

        // Inicializar elementos de la UI
        buttonSave = findViewById(R.id.buttonSave);
        buttonAddToCart = findViewById(R.id.buttonBuy);
        backButton = findViewById(R.id.backButton);
        buttonPreview = findViewById(R.id.buttonPreview);
        textViewTitle = findViewById(R.id.textViewTitle2);
        textViewAuthor = findViewById(R.id.textViewAuthor);
        textViewDescription = findViewById(R.id.textViewDescription);
        imageViewCover = findViewById(R.id.imageViewCover);
        textViewDetails = findViewById(R.id.textViewDetails); // Añadido
        ProgressBar progressBar = findViewById(R.id.progressBar);

        // Mostrar el ProgressBar y deshabilitar botones mientras se verifica
        progressBar.setVisibility(View.VISIBLE);
        buttonAddToCart.setEnabled(false);
        buttonSave.setEnabled(false);

        // Obtener el ID del libro actual
        if (getIntent() != null) {
            bookId = getIntent().getStringExtra("BOOK_ID");
        }

        // Cargar lista de libros comprados
        List<Libro> purchasedBooks = getPurchasedBooks();

        // Cargar detalles del libro
        cargarDetallesDelLibro(bookId, progressBar);

        // Configurar botón para la vista previa del libro
        buttonPreview.setOnClickListener(v -> openPreviewActivity());

        // Verificar si el libro ya está comprado al presionar los botones
        buttonAddToCart.setOnClickListener(v -> {
            if (isBookPurchased(bookId, purchasedBooks)) {
                Toast.makeText(this, "Este libro ya ha sido comprado.", Toast.LENGTH_SHORT).show();
            } else {
                agregarAlCarrito();
            }
        });

        buttonSave.setOnClickListener(v -> {
            if (isBookPurchased(bookId, purchasedBooks)) {
                Toast.makeText(this, "No puedes guardar un libro ya comprado.", Toast.LENGTH_SHORT).show();
            } else {
                guardarLibro();
            }
        });

        backButton.setOnClickListener(v -> finish());
    }


    private boolean isBookPurchased(String bookId, List<Libro> purchasedBooks) {
        for (Libro libro : purchasedBooks) {
            if (libro.getIsbn_lib().equals(bookId)) {
                return true;
            }
        }
        return false;
    }

    private void openPreviewActivity() {
        // Verifica que el bookId no sea nulo antes de abrir el PreviewActivity
        if (bookId != null) {
            Intent intent = new Intent(DescriptionBookActivity.this, PreviewActivity.class);
            intent.putExtra("BOOK_ID", bookId);  // Pasa el bookId al siguiente Activity
            startActivity(intent);  // Inicia PreviewActivity
        } else {
            Toast.makeText(this, "No se pudo cargar la vista previa", Toast.LENGTH_SHORT).show();
        }
    }

    private List<Libro> getPurchasedBooks() {
        SharedPreferences userPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String email = userPrefs.getString("email_user", "");

        SharedPreferences sharedPreferences = getSharedPreferences("PurchasedPrefs", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("purchased_" + email, "[]");

        Gson gson = new Gson();
        Type type = new TypeToken<List<Libro>>() {}.getType();
        return gson.fromJson(json, type);
    }


    private void agregarAlCarrito() {
        if (libroActual == null) return;

        // Obtener el correo del usuario
        SharedPreferences userPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String email = userPrefs.getString("email_user", "");

        if (email.isEmpty()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener SharedPreferences para el carrito del usuario
        SharedPreferences sharedPreferences = getSharedPreferences("CarritoPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Obtener la lista actual de libros del carrito para el usuario específico
        Gson gson = new Gson();
        String json = sharedPreferences.getString("carrito_" + email, "[]");
        Type type = new TypeToken<List<Libro>>() {}.getType();
        List<Libro> carrito = gson.fromJson(json, type);

        // Verificar si el libro ya está en el carrito
        boolean libroYaEnCarrito = false;
        for (Libro libro : carrito) {
            if (libro.getIsbn_lib().equals(libroActual.getIsbn_lib())) {
                libroYaEnCarrito = true;
                break;
            }
        }

        if (libroYaEnCarrito) {
            Toast.makeText(this, "El libro ya está en el carrito", Toast.LENGTH_SHORT).show();
            return;
        }

        // Agregar el libro al carrito y guardar los datos actualizados para el usuario específico
        carrito.add(libroActual);
        String carritoActualizado = gson.toJson(carrito);
        editor.putString("carrito_" + email, carritoActualizado);
        editor.apply();

        Toast.makeText(this, "Libro agregado al carrito", Toast.LENGTH_SHORT).show();
    }

    private void guardarLibro() {
        if (libroActual == null) return;

        // Obtener el correo del usuario
        SharedPreferences userPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String email = userPrefs.getString("email_user", "");

        if (email.isEmpty()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener SharedPreferences para los libros guardados del usuario
        SharedPreferences sharedPreferences = getSharedPreferences("GuardadosPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Obtener la lista actual de libros guardados para el usuario específico
        Gson gson = new Gson();
        String json = sharedPreferences.getString("guardados_" + email, "[]");
        Type type = new TypeToken<List<Libro>>() {}.getType();
        List<Libro> guardados = gson.fromJson(json, type);

        boolean libroYaGuardado = false;
        for (Libro libro : guardados) {
            if (libro.getIsbn_lib().equals(libroActual.getIsbn_lib())) {
                libroYaGuardado = true;
                break;
            }
        }

        if (libroYaGuardado) {
            Toast.makeText(this, "El libro ya está guardado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Agregar el libro a los guardados y guardar los datos actualizados para el usuario específico
        guardados.add(libroActual);
        String guardadosActualizado = gson.toJson(guardados);
        editor.putString("guardados_" + email, guardadosActualizado);
        editor.apply();

        Toast.makeText(this, "Libro guardado", Toast.LENGTH_SHORT).show();
    }


    private void cargarDetallesDelLibro(String bookId, ProgressBar progressBar) {

        //AuthService authService = retrofit.create(AuthService.class);
        AuthService authService = ApiClient.getClientConToken().create(AuthService.class);
        authService.getLibro(bookId).enqueue(new Callback<ApiResponse_L>() {
            @Override
            public void onResponse(Call<ApiResponse_L> call, Response<ApiResponse_L> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse_L apiResponse = response.body();
                    libroActual = apiResponse.getData();

                    if (libroActual != null) {
                        textViewTitle.setText(libroActual.getTitulo_lib());
                        textViewAuthor.setText(libroActual.getNombre_completo());
                        textViewDescription.setText(libroActual.getDescripcion());
                        textViewDetails.setText("¿De qué se trata?");
                        String imageUrl = libroActual.getImage();

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(DescriptionBookActivity.this)
                                    .load(imageUrl)
                                    .into(imageViewCover);
                        } else {
                            imageViewCover.setImageResource(R.drawable.logo_ebook);
                        }

                        // Verificar si el libro está comprado
                        verificarLibroComprado(bookId, progressBar);
                    } else {
                        Log.e("DescriptionBookActivity", "El objeto 'Libro' en data es null");
                    }
                } else {
                    Log.e("API_ERROR", "Error al obtener el libro: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse_L> call, Throwable t) {
                Log.e("API_ERROR", "Fallo en la llamada a la API: ", t);
                progressBar.setVisibility(View.GONE); // Ocultar ProgressBar en caso de error
            }
        });
    }
    private void verificarLibroComprado(String bookId, ProgressBar progressBar) {
        SharedPreferences userPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String email = userPrefs.getString("email_user", "");
        boolean isGoogleUser = userPrefs.getBoolean("is_google_user", false);

        if (isGoogleUser) {
            // Cargar libros comprados desde SharedPreferences
            SharedPreferences purchasedPrefs = getSharedPreferences("LibrosCompradosPrefs", Context.MODE_PRIVATE);
            String json = purchasedPrefs.getString("comprados_" + email, "[]");
            Gson gson = new Gson();
            Type type = new TypeToken<List<Libro>>() {}.getType();
            List<Libro> librosComprados = gson.fromJson(json, type);

            if (librosComprados != null) {
                boolean isPurchased = false;
                for (Libro libro : librosComprados) {
                    if (libro.getIsbn_lib().equals(bookId)) {
                        isPurchased = true;
                        break;
                    }
                }

                actualizarEstadoBotones(isPurchased);
            } else {
                actualizarEstadoBotones(false);
            }
            progressBar.setVisibility(View.GONE);
        } else {
            // Lógica existente para obtener libros comprados desde la API


           //AuthService authService = retrofit.create(AuthService.class);
            AuthService authService = ApiClient.getClientConToken().create(AuthService.class);
            authService.obtenerLibrosComprados(email).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Libro> librosComprados = response.body().getLibros();
                        boolean isPurchased = false;

                        for (Libro libro : librosComprados) {
                            if (libro.getIsbn_lib().equals(bookId)) {
                                isPurchased = true;
                                break;
                            }
                        }

                        actualizarEstadoBotones(isPurchased);
                    } else {
                        Log.e("API_ERROR", "Error al obtener libros comprados: " + response.message());
                        actualizarEstadoBotones(false);
                    }

                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Log.e("API_ERROR", "Fallo en la llamada a la API de libros comprados: ", t);
                    actualizarEstadoBotones(false);
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void actualizarEstadoBotones(boolean isPurchased) {
        if (isPurchased) {
            buttonAddToCart.setEnabled(false);
            buttonSave.setEnabled(false);
            Toast.makeText(this, "Este libro ya ha sido comprado.", Toast.LENGTH_SHORT).show();
        } else {
            buttonAddToCart.setEnabled(true);
            buttonSave.setEnabled(true);
        }
    }


}