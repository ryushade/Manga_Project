package com.example.manga_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.manga_project.databinding.ActivityOlvidarPasswordBinding;

public class OlvidarPasswordActivity extends AppCompatActivity {
    private ActivityOlvidarPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOlvidarPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSendLink.setOnClickListener(v -> {
            String email = binding.edtEmailForgot.getText().toString();
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa tu correo electrónico", Toast.LENGTH_SHORT).show();
            } else {
                // Aquí deberías implementar el envío real del enlace de recuperación
                Toast.makeText(this, "Se ha enviado un enlace a tu correo si existe en el sistema", Toast.LENGTH_LONG).show();
            }
        });

        binding.lblBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}

