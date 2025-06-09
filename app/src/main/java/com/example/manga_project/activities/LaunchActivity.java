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

import com.example.manga_project.R;
import com.example.manga_project.databinding.ActivityLaunchBinding;

public class LaunchActivity extends AppCompatActivity {

    private ActivityLaunchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityLaunchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnEmpezar.setOnClickListener(v -> {
            if (isUserAuthenticated()) {
                redirigirSegunRol();
            } else {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });
    }

    private boolean isUserAuthenticated() {
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        return token != null && !token.isEmpty();
    }

    private void redirigirSegunRol() {
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        int idRol = prefs.getInt("id_rol", -1);  // ¡ojo! la clave correcta es "id_rol", no "idRol"

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
