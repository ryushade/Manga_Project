package com.example.manga_project.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.R;
import com.example.manga_project.databinding.ActivityLaunchBinding;

/**
 * Pantalla inicial: decide si mostrar Login o la vista del rol correspondiente.
 */
public class LaunchActivity extends AppCompatActivity {

    private ActivityLaunchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // 1. ViewBinding
        binding = ActivityLaunchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Ajuste de insets (barra de estado, navegación)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 3. ***Inicializar ApiClient con contexto global***
        ApiClient.setContext(getApplicationContext());

        // ───────── OPCIONAL: elegir backend local o remoto ─────────
        // Puedes guardar esta preferencia en SharedPreferences o BuildConfig.
        boolean usarLocal = true;   // ← cámbialo según necesidad
        ApiClient.usarBackendLocal(usarLocal);
        // ───────────────────────────────────────────────────────────

        // 4. Botón «Empezar»
        binding.btnEmpezar.setOnClickListener(v -> {
            if (isUserAuthenticated()) {
                redirigirSegunRol();
            } else {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });
    }

    /** Devuelve true si hay un JWT almacenado. */
    private boolean isUserAuthenticated() {
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        return token != null && !token.isEmpty();
    }

    /** Redirige a la actividad apropiada según el id_rol guardado. */
    private void redirigirSegunRol() {
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        int idRol = prefs.getInt("id_rol", -1);   // clave «id_rol»

        Intent intent;
        switch (idRol) {
            case 3:
                intent = new Intent(this, MainAdminActivity.class);
                break;
            case 2:
                intent = new Intent(this, MainProveedorActivity.class);
                break;
            case 1:
            default:
                intent = new Intent(this, MainActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }
}
