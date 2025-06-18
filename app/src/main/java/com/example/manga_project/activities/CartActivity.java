package com.example.manga_project.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.google.gson.Gson;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
// -- no importar okhttp3.Response --
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;  // retrofit2.Response para las callbacks

public class CartActivity extends AppCompatActivity {

    private static final String TAG = "CartActivity";

    private RecyclerView recyclerViewBooks;
    private TextView totalAmountTextView;
    private Button payWithStripeButton;

    private List<ItemCarrito> carritoItems = new ArrayList<>();
    private CartAdapterCarrito carritoAdapter;

    private SharedPreferences sharedPreferences;
    private Gson gson;

    private AuthService api;

    // Stripe
    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;
    private PaymentSheet.CustomerConfiguration customerConfig;

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

        // Stripe
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        inicializarPagoStripe();

        payWithStripeButton.setOnClickListener(v -> presentarHojaDePago());
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
                    carritoItems.remove(position);
                    carritoAdapter.notifyItemRemoved(position);
                    actualizarTotal();
                    Toast.makeText(CartActivity.this, "Eliminado del carrito", Toast.LENGTH_SHORT).show();
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

    private void inicializarPagoStripe() {
        int totalCentimos = 0;
        for (ItemCarrito it : carritoItems) {
            totalCentimos += (int)(it.precio_unit * it.cantidad * 100);
        }

        String url   = "https://grupo1damb.pythonanywhere.com/payment-sheet/" + totalCentimos;
        String token = sharedPreferences.getString("token","");

        OkHttpClient client = new OkHttpClient();
        RequestBody emptyBody = RequestBody.create(
                "", MediaType.parse("application/json")
        );
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization","Bearer "+token)
                .post(emptyBody)
                .build();

        new Thread(() -> {
            try (okhttp3.Response resp = client.newCall(request).execute()) {
                if (resp.isSuccessful() && resp.body() != null) {
                    String json = resp.body().string();
                    JSONObject obj = new JSONObject(json);
                    runOnUiThread(() -> {
                        try {
                            customerConfig = new PaymentSheet.CustomerConfiguration(
                                    obj.getString("customer"),
                                    obj.getString("ephemeralKey")
                            );
                            paymentIntentClientSecret = obj.getString("paymentIntent");
                            PaymentConfiguration.init(
                                    getApplicationContext(),
                                    obj.getString("publishableKey")
                            );
                        } catch (JSONException e) {
                            Log.e(TAG,"JSON error",e);
                        }
                    });
                } else {
                    Log.e(TAG,"Stripe request failed: "+resp);
                }
            } catch (Exception e) {
                Log.e(TAG,"Error en inicializarPagoStripe",e);
            }
        }).start();
    }

    private void presentarHojaDePago() {
        if (carritoItems.isEmpty()) {
            Toast.makeText(this,"El carrito está vacío",Toast.LENGTH_SHORT).show();
            return;
        }

        PaymentSheet.GooglePayConfiguration gpayConfig =
                new PaymentSheet.GooglePayConfiguration(
                        PaymentSheet.GooglePayConfiguration.Environment.Test, "US"
                );

        PaymentSheet.Configuration config =
                new PaymentSheet.Configuration.Builder("Example, Inc.")
                        .customer(customerConfig)
                        .allowsDelayedPaymentMethods(true)
                        .googlePay(gpayConfig)
                        .build();

        paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret, config
        );
    }

    private void onPaymentSheetResult(PaymentSheetResult result) {
        if (result instanceof PaymentSheetResult.Canceled) {
            Log.d(TAG,"Pago cancelado");
        } else if (result instanceof PaymentSheetResult.Failed) {
            Log.e(TAG,"Error de pago", ((PaymentSheetResult.Failed) result).getError());
            Toast.makeText(this,"El pago ha fallado",Toast.LENGTH_SHORT).show();
        } else if (result instanceof PaymentSheetResult.Completed) {
            Log.d(TAG,"Pago completado");
            Toast.makeText(this,"Pago completado",Toast.LENGTH_SHORT).show();
            procesarVenta();
        }
    }

    private void procesarVenta() {
        new Handler().postDelayed(() -> {
            carritoItems.clear();
            carritoAdapter.notifyDataSetChanged();
            actualizarTotal();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 3000);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
