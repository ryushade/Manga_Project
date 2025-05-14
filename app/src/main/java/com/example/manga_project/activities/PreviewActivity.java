package com.example.manga_project.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.manga_project.R;

public class PreviewActivity extends AppCompatActivity {

    private WebView webView;
    private LinearLayout overlayLayer;
    private TextView messageText;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "myPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        webView = findViewById(R.id.webView);
        overlayLayer = findViewById(R.id.overlayLayer);
        messageText = findViewById(R.id.messageText);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // Obtener el correo del usuario
        String emailUser = sharedPreferences.getString("email_user", null);
        String bookId = getIntent().getStringExtra("BOOK_ID");

        // Depuración: Mostrar en Log si el correo y el ID del libro no están null
        Log.d("PreviewActivity", "Email User: " + emailUser);
        Log.d("PreviewActivity", "Book ID: " + bookId);

        if (bookId != null && emailUser != null) {
            checkAndLoadBookPreview(bookId, emailUser);
        } else {
            Log.e("PreviewActivity", "El correo del usuario o el ID del libro son nulos.");
            showToast("Error: No se pudo cargar la vista previa del libro.");
        }
    }

    // Método para verificar y cargar la vista previa del libro
    private void checkAndLoadBookPreview(String bookId, String emailUser) {
        String key = "has_visited_" + emailUser + "_" + bookId;
        boolean hasVisited = sharedPreferences.getBoolean(key, false);

        if (hasVisited) {
            showToast("¡El tiempo de prueba ha terminado!");
            showOverlay();
        } else {
            showToast("El tiempo de muestra es limitado. ¡Compra el libro para seguir leyendo!");

            new Thread(() -> {
                try {
                    Thread.sleep(50000); // Espera 50 segundos antes de mostrar la capa
                    runOnUiThread(() -> showOverlay());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            // Marcar como visitado el libro para este usuario
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(key, true); // Marca el libro como visitado por el usuario
            editor.apply();
        }

        // Cargar la vista previa del libro
        loadBookPreview(bookId);
    }

    // Método para cargar el archivo EPUB
    private void loadBookPreview(String bookId) {
        String webReaderLink = "https://www.pdfbooksworld.com/bibi/pre.html?book=" + bookId + ".epub";
        webView.loadUrl(webReaderLink);

        // Restaurar la posición previa del libro
        float savedPosition = sharedPreferences.getFloat("book_position_" + bookId, 0);
        webView.scrollTo(0, (int) savedPosition); // Restaurar la posición
    }

    // Mostrar la capa de bloqueo
    private void showOverlay() {
        overlayLayer.setVisibility(View.VISIBLE);
        webView.setVisibility(View.INVISIBLE);
    }

    // Método para mostrar Toasts
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Guardar la posición actual del libro
    @Override
    public void onPause() {
        super.onPause();
        float position = webView.getScrollY();
        String bookId = getIntent().getStringExtra("BOOK_ID");
        if (bookId != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("book_position_" + bookId, position);
            editor.apply();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
