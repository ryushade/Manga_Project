package com.example.manga_project.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.ListarCarritoResponse;
import com.example.manga_project.Modelos.ListarCarritoResponse.ItemCarrito;
import com.example.manga_project.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PagoTarjetaActivity extends AppCompatActivity {
    private TextInputEditText etNombre, etNumero, etVencimiento, etCvv;
    private MaterialButton btnConfirmar;
    private AuthService api;
    private SharedPreferences sharedPreferences;
    private List<ItemCarrito> carritoItems = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pago_tarjeta);

        etNombre = findViewById(R.id.etNombre);
        etNumero = findViewById(R.id.etNumero);
        etVencimiento = findViewById(R.id.etVencimiento);
        etCvv = findViewById(R.id.etCvv);
        btnConfirmar = findViewById(R.id.btnConfirmarPago);

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        api = ApiClient.getClientConToken().create(AuthService.class);

        // Recibe el carrito por intent (puedes cambiar esto según tu lógica)
        if (getIntent().hasExtra("carrito")) {
            carritoItems = (ArrayList<ItemCarrito>) getIntent().getSerializableExtra("carrito");
        }

        // El flujo de pago tradicional ha sido eliminado. El botón solo valida datos y debe delegar el pago a Stripe.
        btnConfirmar.setOnClickListener(v -> {
            String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
            String numero = etNumero.getText() != null ? etNumero.getText().toString().trim() : "";
            String vencimiento = etVencimiento.getText() != null ? etVencimiento.getText().toString().trim() : "";
            String cvv = etCvv.getText() != null ? etCvv.getText().toString().trim() : "";

            if (nombre.isEmpty() || numero.isEmpty() || vencimiento.isEmpty() || cvv.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            // Aquí solo deberías iniciar el flujo de pago con Stripe
            // Por ejemplo: iniciarPagoStripe(nombre, numero, vencimiento, cvv, carritoItems);
            Toast.makeText(this, "Implementa aquí el flujo de pago con Stripe", Toast.LENGTH_SHORT).show();
        });
    }
}
