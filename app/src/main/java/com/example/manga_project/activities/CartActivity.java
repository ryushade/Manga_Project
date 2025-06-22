package com.example.manga_project.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.ListarCarritoResponse;
import com.example.manga_project.Modelos.ListarCarritoResponse.ItemCarrito;
import com.example.manga_project.Modelos.RespuestaGenerica;
import com.example.manga_project.R;
import com.example.manga_project.adapters.CartAdapterCarrito;
import com.example.manga_project.fragments.PagoTarjetaFragment;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;  // retrofit2.Response para las callbacks

public class CartActivity extends AppCompatActivity {

    private static final String TAG = "CartActivity";

    private RecyclerView recyclerViewBooks;
    private TextView totalAmountTextView;
    private Button payWithStripeButton;
    private AlertDialog paymentDialog;

    private List<ItemCarrito> carritoItems = new ArrayList<>();
    private CartAdapterCarrito carritoAdapter;

    private SharedPreferences sharedPreferences;
    private Gson gson;

    private AuthService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Toolbar con flecha atrás
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ico_back);
        }

        recyclerViewBooks   = findViewById(R.id.recyclerViewBooks);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        payWithStripeButton = findViewById(R.id.payWithStripeButton);
        payWithStripeButton.setEnabled(true); // Siempre habilitado
        payWithStripeButton.setOnClickListener(v -> mostrarModalPagoTarjeta());

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        gson = new Gson();

        api = ApiClient.getClientConToken().create(AuthService.class);

        // Configura RecyclerView
        recyclerViewBooks.setLayoutManager(new LinearLayoutManager(this));
        carritoAdapter = new CartAdapterCarrito(
                carritoItems,
                (item, position) -> eliminarItemBackend(item.id_volumen, position),
                api
        );
        recyclerViewBooks.setAdapter(carritoAdapter);

        cargarCarritoBackend();
    }

    private void cargarCarritoBackend() {
        api.listarCarrito().enqueue(new Callback<ListarCarritoResponse>() {
            @Override
            public void onResponse(Call<ListarCarritoResponse> call, Response<ListarCarritoResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().code == 0) {
                    carritoItems.clear();
                    carritoItems.addAll(response.body().items);
                    carritoAdapter.notifyDataSetChanged();
                    actualizarTotal(response.body().total);
                } else {
                    Toast.makeText(CartActivity.this, "No se pudo cargar el carrito", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ListarCarritoResponse> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Error de red al cargar carrito", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "cargarCarritoBackend", t);
            }
        });
    }

    private void eliminarItemBackend(int idVolumen, int position) {
        api.eliminarItemCarrito(idVolumen).enqueue(new Callback<RespuestaGenerica>() {
            @Override
            public void onResponse(Call<RespuestaGenerica> call, Response<RespuestaGenerica> response) {
                if (response.isSuccessful() && response.body() != null && response.body().code == 0) {
                    if (position >= 0 && position < carritoItems.size()) {
                        carritoItems.remove(position);
                        carritoAdapter.notifyItemRemoved(position);
                        actualizarTotal();
                        Toast.makeText(CartActivity.this, "Eliminado del carrito", Toast.LENGTH_SHORT).show();
                    } else {
                        cargarCarritoBackend(); // Recarga el carrito si el índice no es válido
                    }
                } else {
                    Toast.makeText(CartActivity.this, "No se pudo eliminar del carrito", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<RespuestaGenerica> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Error de red al eliminar", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "eliminarItemBackend", t);
            }
        });
    }

    private void actualizarTotal() {
        double total = 0;
        for (ItemCarrito it : carritoItems) {
            total += it.precio_unit * it.cantidad;
        }
        totalAmountTextView.setText(
                String.format(Locale.getDefault(), "Total: S/. %.2f", total)
        );
    }

    private void actualizarTotal(double servidorTotal) {
        totalAmountTextView.setText(
                String.format(Locale.getDefault(), "Total: S/. %.2f", servidorTotal)
        );
    }

    private void mostrarModalPagoTarjeta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.layout_payment_options, null);
        builder.setView(dialogView);
        paymentDialog = builder.create();
        paymentDialog.show();

        MaterialButton tarjetaBtn = dialogView.findViewById(R.id.other_payment_button);
        tarjetaBtn.setOnClickListener(v -> {
            paymentDialog.dismiss();
            // Navegar a un fragmento para llenar los datos de la tarjeta
            getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new PagoTarjetaFragment())
                .addToBackStack(null)
                .commit();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
