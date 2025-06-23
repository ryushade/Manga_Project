package com.example.manga_project.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.manga_project.Modelos.GuardarVentaResponse;
import com.example.manga_project.Modelos.ListarCarritoResponse;
import com.example.manga_project.Modelos.ListarCarritoResponse.ItemCarrito;
import com.example.manga_project.Modelos.RespuestaGenerica;
import com.example.manga_project.Modelos.StripePaymentSheetResponse;
import com.example.manga_project.R;
import com.example.manga_project.adapters.CartAdapterCarrito;
import com.example.manga_project.fragments.PagoTarjetaFragment;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

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

    // Stripe PaymentSheet
    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;
    private String stripeCustomerId;
    private String stripeEphemeralKey;
    private String stripePublishableKey;
    private boolean isPaymentSheetReady = false;

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
        payWithStripeButton.setEnabled(false); // Deshabilitado hasta que esté listo
        payWithStripeButton.setOnClickListener(v -> launchStripePaymentSheet());

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        gson = new Gson();

        api = ApiClient.getClientConToken().create(AuthService.class);

        // Inicializar PaymentSheet de Stripe
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        // Configura RecyclerView
        recyclerViewBooks.setLayoutManager(new LinearLayoutManager(this));
        carritoAdapter = new CartAdapterCarrito(
                carritoItems,
                (item, position) -> eliminarItemBackend(item.id_volumen, position),
                api
        );
        recyclerViewBooks.setAdapter(carritoAdapter);

        cargarCarritoYPrepararPago();
    }

    private void cargarCarritoYPrepararPago() {
        api.listarCarrito().enqueue(new retrofit2.Callback<ListarCarritoResponse>() {
            @Override
            public void onResponse(Call<ListarCarritoResponse> call, retrofit2.Response<ListarCarritoResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().code == 0) {
                    carritoItems.clear();
                    carritoItems.addAll(response.body().items);
                    carritoAdapter.notifyDataSetChanged();
                    actualizarTotal(response.body().total);
                    prepararStripePaymentSheet();
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

    private void prepararStripePaymentSheet() {
        // Sumar el total del carrito
        int totalCents = calcularTotalCarritoCents();
        if (totalCents <= 0) {
            payWithStripeButton.setEnabled(false);
            return;
        }
        payWithStripeButton.setEnabled(false);
        // Llamar a backend para obtener datos de Stripe
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("amount_cents", totalCents);
        api.getStripePaymentSheet(body).enqueue(new retrofit2.Callback<StripePaymentSheetResponse>() {
            @Override
            public void onResponse(retrofit2.Call<StripePaymentSheetResponse> call, retrofit2.Response<StripePaymentSheetResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StripePaymentSheetResponse data = response.body();
                    stripePublishableKey = data.publishableKey;
                    stripeCustomerId = data.customer;
                    stripeEphemeralKey = data.ephemeralKey;
                    paymentIntentClientSecret = data.paymentIntent;
                    PaymentConfiguration.init(getApplicationContext(), stripePublishableKey);
                    isPaymentSheetReady = true;
                    payWithStripeButton.setEnabled(true);
                } else {
                    mostrarError("Error al preparar el pago. Intenta más tarde.");
                }
            }
            @Override
            public void onFailure(retrofit2.Call<StripePaymentSheetResponse> call, Throwable t) {
                mostrarError("No se pudo conectar con el servidor de pagos.");
            }
        });
    }

    private int calcularTotalCarritoCents() {
        int total = 0;
        for (ItemCarrito item : carritoItems) {
            total += (int) (item.precio_unit * item.cantidad * 100); // Usar precio_unit y cantidad
        }
        return total;
    }

    private void eliminarItemBackend(int idVolumen, int position) {
        api.eliminarItemCarrito(idVolumen).enqueue(new retrofit2.Callback<RespuestaGenerica>() {
            @Override
            public void onResponse(Call<RespuestaGenerica> call, retrofit2.Response<RespuestaGenerica> response) {
                if (response.isSuccessful() && response.body() != null && response.body().code == 0) {
                    if (position >= 0 && position < carritoItems.size()) {
                        carritoItems.remove(position);
                        carritoAdapter.notifyItemRemoved(position);
                        actualizarTotal();
                        Toast.makeText(CartActivity.this, "Eliminado del carrito", Toast.LENGTH_SHORT).show();
                    } else {
                        cargarCarritoYPrepararPago(); // Recarga el carrito si el índice no es válido
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
            // Navegar a un fragmento para llenar los datos de la tarjeta con el carrito
            getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content,
                    PagoTarjetaFragment.newInstance(new ArrayList<>(carritoItems)))
                .addToBackStack(null)
                .commit();
        });

        MaterialButton googlePayBtn = dialogView.findViewById(R.id.google_pay_button);
        googlePayBtn.setOnClickListener(v -> {
            paymentDialog.dismiss();
            iniciarPagoGooglePay();
        });
    }

    private void iniciarPagoGooglePay() {
        // Calcular el total en centavos
        double total = 0;
        for (ItemCarrito it : carritoItems) {
            total += it.precio_unit * it.cantidad;
        }
        int amountCents = (int) Math.round(total * 100);
        if (amountCents <= 0) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }
        // Llamar al endpoint /payment-sheet
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                JSONObject json = new JSONObject();
                json.put("amount_cents", amountCents);
                String token = sharedPreferences.getString("token", "");
                RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
                Request request = new Request.Builder()
                        .url(ApiClient.BASE_URL_REMOTA + "/payment-sheet")
                        .addHeader("Authorization", "Bearer " + token)
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String respStr = response.body().string();
                    JSONObject respJson = new JSONObject(respStr);
                    paymentIntentClientSecret = respJson.getString("paymentIntent");
                    String publishableKey = respJson.getString("publishableKey");
                    runOnUiThread(() -> mostrarStripePaymentSheet(publishableKey));
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Error al iniciar pago", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error de red: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void mostrarStripePaymentSheet(String publishableKey) {
        PaymentConfiguration.init(this, publishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        PaymentSheet.Configuration config = new PaymentSheet.Configuration.Builder("Manga Project")
                .googlePay(new PaymentSheet.GooglePayConfiguration(
                        PaymentSheet.GooglePayConfiguration.Environment.Test, // Cambia a .Production en producción
                        "PE" // País
                ))
                .build();
        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, config);
    }

    private void launchStripePaymentSheet() {
        if (!isPaymentSheetReady) return;
        PaymentSheet.CustomerConfiguration customerConfig = new PaymentSheet.CustomerConfiguration(
                stripeCustomerId, stripeEphemeralKey
        );
        PaymentSheet.Configuration config = new PaymentSheet.Configuration.Builder("Manga Store")
                .customer(customerConfig)
                .allowsDelayedPaymentMethods(true)
                .build();
        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, config);
    }

    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Guardar la venta en el backend
            guardarVentaEnBackend();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            mostrarMensaje("Pago cancelado.");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            mostrarError("El pago falló. Intenta de nuevo.");
        }
    }

    private void guardarVentaEnBackend() {
        // Construir el carrito para enviar al backend
        List<Map<String, Object>> carritoParaBackend = new ArrayList<>();
        for (ItemCarrito item : carritoItems) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id_volumen", item.id_volumen);
            map.put("cantidad", item.cantidad);
            map.put("precio_unit", item.precio_unit);
            carritoParaBackend.add(map);
        }
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("carrito", carritoParaBackend);
        body.put("payment_intent_id", paymentIntentClientSecret != null ? paymentIntentClientSecret.split("_secret")[0] : "");

        String token = sharedPreferences.getString("token", "");
        api.guardarVenta(body, "Bearer " + token).enqueue(new retrofit2.Callback<GuardarVentaResponse>() {
            @Override
            public void onResponse(retrofit2.Call<GuardarVentaResponse> call, retrofit2.Response<GuardarVentaResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().code == 0) {
                    mostrarMensaje("¡Pago realizado y venta registrada!");
                    irADetallePagoExitoso(response.body().id_venta);
                } else {
                    mostrarError("Pago realizado, pero error al registrar la venta.");
                }
            }
            @Override
            public void onFailure(retrofit2.Call<GuardarVentaResponse> call, Throwable t) {
                mostrarError("Pago realizado, pero error de red al registrar la venta.");
            }
        });
    }

    private void irADetallePagoExitoso(int idVenta) {
        Intent intent = new Intent(this, DetalleOrdenActivity.class);
        intent.putExtra("id_venta", idVenta);
        intent.putExtra("historietas", new ArrayList<>(carritoItems));
        startActivity(intent);
        finish();
    }

    private void mostrarMensaje(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    private void mostrarError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        payWithStripeButton.setEnabled(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
