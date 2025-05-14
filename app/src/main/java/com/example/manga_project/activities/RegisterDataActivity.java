package com.example.manga_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.ApiReniec;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Api_cliente.DniResponse;
import com.example.manga_project.Api_cliente.DniRucApiService;
import com.example.manga_project.Modelos.RegisterResponse;
import com.example.manga_project.Modelos.RegisterRequest;
import com.example.manga_project.databinding.ActivityRegisterDataBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterDataActivity extends AppCompatActivity {

    private ActivityRegisterDataBinding binding;
    private static final String TOKEN_CLIENTE = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6ImJ1c3RhbWFudGU3NzdhQGdtYWlsLmNvbSJ9.0tadscJV_zWQqZeRMDM4XEQ9_t0f7yph4WJWNoyDHyw";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.registerButton.setOnClickListener(v -> registerUser());

        // Configura el botón de búsqueda de DNI
        binding.dniSearchButton.setOnClickListener(v -> {
            String dni = binding.txtDni.getText().toString();
            if (!dni.isEmpty()) {
                fetchUserByDni(dni);
            } else {
                Toast.makeText(this, "Por favor ingresa un DNI", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserByDni(String dni) {
        DniRucApiService apiService = ApiReniec.getApiService();
        Call<DniResponse> call = apiService.getUserByDni(dni, TOKEN_CLIENTE);

        call.enqueue(new Callback<DniResponse>() {
            @Override
            public void onResponse(@NonNull Call<DniResponse> call, @NonNull Response<DniResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DniResponse dniData = response.body();
                    // Autocompleta los campos de nombres y apellidos en el formulario de registro
                    binding.txtNames.setText(dniData.getNombres());
                    binding.txtLastNames.setText(dniData.getApellidos());
                } else {
                    Toast.makeText(RegisterDataActivity.this, "No se encontraron datos para el DNI ingresado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DniResponse> call, @NonNull Throwable t) {
                Toast.makeText(RegisterDataActivity.this, "Error en la conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser() {
        // Verificar que el usuario acepte los términos y condiciones
        if (!binding.termsCheckbox.isChecked()) {
            Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
            return;
        }

        String fechaInput = binding.txtDate.getText().toString();
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String fechaFormateada = "";

        try {
            Date date = inputFormat.parse(fechaInput);
            fechaFormateada = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Formato de fecha inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear el objeto de solicitud de registro
        AuthService authService = ApiClient.getClientSinToken().create(AuthService.class);
        RegisterRequest registerRequest = new RegisterRequest(
                binding.txtEmail.getText().toString(),
                binding.txtContrasena.getText().toString(),
                binding.txtDni.getText().toString(),
                binding.txtNames.getText().toString(),
                binding.txtLastNames.getText().toString(),
                fechaFormateada
        );

        // Enviar la solicitud de registro
        authService.register(registerRequest).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterDataActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    navigateToLoginScreen();
                } else {
                    Toast.makeText(RegisterDataActivity.this, "Error en el registro", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                Toast.makeText(RegisterDataActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToLoginScreen() {
        Intent intent = new Intent(RegisterDataActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Cierra RegisterActivity para que no quede en el stack
    }
}