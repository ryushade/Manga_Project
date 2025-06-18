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
import com.example.manga_project.Api_cliente.Libro;
import com.example.manga_project.R;
import com.example.manga_project.adapters.CartAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray; // Asegúrate de importar esta clase
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MediaType;

public class CartActivity extends AppCompatActivity {

    private static final String TAG = "CartActivity";
    private RecyclerView recyclerViewBooks;
    private CartAdapter cartAdapter;
    private List<Libro> bookList;
    private TextView totalAmountTextView;
    private Button payWithStripeButton;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    // Variables para Stripe
    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;
    private PaymentSheet.CustomerConfiguration customerConfig;

    private ArrayList<Libro> librosComprados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Configurar Toolbar con flecha de retroceso
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ico_back); // usa tu drawable
        }

        recyclerViewBooks = findViewById(R.id.recyclerViewBooks);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        payWithStripeButton = findViewById(R.id.payWithStripeButton);

        // Configurar SharedPreferences y Gson
        sharedPreferences = getSharedPreferences("CarritoPrefs", Context.MODE_PRIVATE);
        gson = new Gson();

        // Cargar libros del carrito
        bookList = cargarCarrito();

        // Configurar el RecyclerView
        configurarRecyclerView();

        // Mostrar el total inicial
        actualizarTotal();

        // Configurar Stripe PaymentSheet
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        inicializarPagoStripe();

        // Configurar el botón de pago con Stripe
        payWithStripeButton.setOnClickListener(v -> presentarHojaDePago());
    }

    private List<Libro> cargarCarrito() {
        // Obtener el correo del usuario
        SharedPreferences userPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String email = userPrefs.getString("email_user", "");

        if (email.isEmpty()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return new ArrayList<>();
        }

        // Cargar el carrito específico del usuario
        SharedPreferences sharedPreferences = getSharedPreferences("CarritoPrefs", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("carrito_" + email, "[]");
        Type type = new TypeToken<List<Libro>>() {}.getType();
        List<Libro> libros = gson.fromJson(json, type);

        return libros != null ? libros : new ArrayList<>();
    }


    private void configurarRecyclerView() {
        recyclerViewBooks.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(bookList, this::eliminarLibroDelCarrito);
        recyclerViewBooks.setAdapter(cartAdapter);
    }

    private void eliminarLibroDelCarrito(Libro libro) {
        bookList.remove(libro);
        guardarCarrito();
        cartAdapter.notifyDataSetChanged();
        actualizarTotal();
    }

    private void guardarCarrito() {
        // Obtener el correo del usuario
        SharedPreferences userPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String email = userPrefs.getString("email_user", "");

        if (email.isEmpty()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar el carrito específico del usuario
        SharedPreferences sharedPreferences = getSharedPreferences("CarritoPrefs", Context.MODE_PRIVATE);
        String json = gson.toJson(bookList);
        sharedPreferences.edit().putString("carrito_" + email, json).apply();
    }


    private void actualizarTotal() {
        double total = 0.0;
        for (Libro libro : bookList) {
            total += libro.getPrecio_lib();
        }
        totalAmountTextView.setText(String.format("Total: S/. %.2f", total));
    }

    private void inicializarPagoStripe() {
        // Calcula el total en céntimos
        double total = 0.0;
        for (Libro libro : bookList) {
            total += libro.getPrecio_lib();
        }
        int totalEnCentimos = (int) (total * 100);  // Convierte a céntimos

        // Construye la URL con el total en céntimos
        String url = "https://grupo1damb.pythonanywhere.com/payment-sheet/" + totalEnCentimos;

        // Obtén el token desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");

        // Usa OkHttpClient para la solicitud
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)  // Añadir el token JWT en el encabezado
                .post(RequestBody.create("", null))  // Se usa un cuerpo vacío para una solicitud POST
                .build();

        // Ejecuta la solicitud en un hilo separado
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JSONObject result = new JSONObject(responseData);

                    // Actualiza los detalles de PaymentSheet en el hilo principal
                    runOnUiThread(() -> {
                        try {
                            customerConfig = new PaymentSheet.CustomerConfiguration(
                                    result.getString("customer"),
                                    result.getString("ephemeralKey")
                            );
                            paymentIntentClientSecret = result.getString("paymentIntent");
                            PaymentConfiguration.init(getApplicationContext(), result.getString("publishableKey"));
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON error", e);
                        }
                    });
                } else {
                    Log.e(TAG, "Request failed: " + response);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error en la solicitud", e);
            }
        }).start();
    }



    private void presentarHojaDePago() {
        if (bookList.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío. No se puede proceder al pago.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Configuración de Google Pay
        PaymentSheet.GooglePayConfiguration googlePayConfiguration =
                new PaymentSheet.GooglePayConfiguration(
                        PaymentSheet.GooglePayConfiguration.Environment.Test,  // Establece el entorno como prueba
                        "US"  // El código de país de tu negocio
                );

        // Configuración de PaymentSheet
        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("Example, Inc.")
                .customer(customerConfig)
                .allowsDelayedPaymentMethods(true)
                .googlePay(googlePayConfiguration)  // Configura Google Pay correctamente
                .build();

        // Presenta la hoja de pago
        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration);
    }


    // Este método se llama cuando el pago con Stripe es exitoso
    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.d(TAG, "Pago cancelado");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Log.e(TAG, "Error de pago: ", ((PaymentSheetResult.Failed) paymentSheetResult).getError());
            Toast.makeText(this, "El pago ha fallado", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Log.d(TAG, "Pago completado con éxito");
            Toast.makeText(this, "Pago completado", Toast.LENGTH_SHORT).show();
            // Llamar al método para guardar la venta en la base de datos
            if (librosComprados == null) {
                librosComprados = new ArrayList<>();
            }
            librosComprados.addAll(bookList);
            JSONArray librosJsonArray = new JSONArray();
            try {
                for (Libro libro : librosComprados) {
                    JSONObject libroJson = new JSONObject();
                    libroJson.put("isbn_lib", libro.getIsbn_lib());
                    libroJson.put("titulo", libro.getTitulo_lib());
                    libroJson.put("autor", libro.getNombre_completo());
                    libroJson.put("precio", libro.getPrecio_lib());
                    // Agregar el JSONObject del libro al JSONArray
                    librosJsonArray.put(libroJson);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error creando el JSON para obtener libros", e);
                return;
            }

            SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
            String emailUser = sharedPreferences.getString("email_user", null);
            enviarCorreoConPdf(emailUser, librosJsonArray);

            guardarVentaEnBaseDeDatos();

            
            // Retrasar la redirección a MainActivity por 3 segundos (3000 milisegundos)
            new Handler().postDelayed(() -> {
                // Vaciar el carrito y guardar el estado
                bookList.clear();
                guardarCarrito();

                // Redirigir a MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }, 3000); // 3000 milisegundos = 3 segundos
        }
    }

    private void guardarVentaEnBaseDeDatos() {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String emailUser = sharedPreferences.getString("email_user", null);

        if (emailUser == null) {
            Log.e(TAG, "Correo del usuario no encontrado en SharedPreferences");
            runOnUiThread(() -> Toast.makeText(this, "Error: Correo no encontrado. No se puede guardar la venta.", Toast.LENGTH_SHORT).show());
            return;
        }

        boolean isGoogleUser = sharedPreferences.getBoolean("is_google_user", false); // Verifica si es usuario de Google


        // Obtener el token JWT desde SharedPreferences
        String token = sharedPreferences.getString("token", "");


        if (isGoogleUser) {
            // Guarda los libros en SharedPreferences en lugar de llamar al backend
            SharedPreferences purchasedPrefs = getSharedPreferences("LibrosCompradosPrefs", Context.MODE_PRIVATE);
            String json = gson.toJson(bookList);
            purchasedPrefs.edit().putString("comprados_" + emailUser, json).apply();

            Log.d(TAG, "Libros comprados guardados en SharedPreferences para usuario de Google");
            runOnUiThread(() -> Toast.makeText(this, "Venta guardada localmente (Google User)", Toast.LENGTH_SHORT).show());
        } else {
            // Crear el cliente HTTP y preparar la solicitud para obtener dni_lec
            OkHttpClient client = new OkHttpClient();
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("email_user", emailUser);
            } catch (JSONException e) {
                Log.e(TAG, "Error creando el JSON para obtener dni_lec", e);
                return;
            }

            RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:5000/api_obtener_dni_lec")
                    .addHeader("Authorization", "Bearer " + token)  // Agregar el token JWT en el encabezado
                    .post(body)
                    .build();

            // Ejecutar la solicitud en un hilo separado
            new Thread(() -> {
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = response.body().string();
                        JSONObject result = new JSONObject(responseData);

                        if (result.getInt("code") == 0) {
                            String dniLec = result.getString("dni_lec");

                            // Preparar la solicitud para guardar la venta
                            JSONObject ventaJsonBody = new JSONObject();
                            try {
                                ventaJsonBody.put("id_user", dniLec);
                                ventaJsonBody.put("total", calcularTotalEnCentimos());

                                // Crear un JSONArray para el carrito
                                JSONArray carritoArray = new JSONArray();
                                for (Libro libro : bookList) {
                                    JSONObject item = new JSONObject();
                                    item.put("isbn_lib", libro.getIsbn_lib());
                                    item.put("precio", libro.getPrecio_lib());
                                    carritoArray.put(item);
                                }

                                ventaJsonBody.put("carrito", carritoArray);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error creando el JSON para guardar la venta", e);
                                return;
                            }

                            // Crear y ejecutar la solicitud para guardar la venta
                            RequestBody ventaBody = RequestBody.create(ventaJsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
                            Request ventaRequest = new Request.Builder()
                                    .url("https://grupo1damb.pythonanywhere.com/api_guardar_venta")
                                    .addHeader("Authorization", "Bearer " + token)
                                    .post(ventaBody)
                                    .build();

                            try (Response ventaResponse = client.newCall(ventaRequest).execute()) {
                                if (ventaResponse.isSuccessful()) {
                                    Log.d(TAG, "Venta guardada exitosamente");
                                    runOnUiThread(() -> Toast.makeText(this, "Venta guardada exitosamente", Toast.LENGTH_SHORT).show());
                                } else {
                                    Log.e(TAG, "Error al guardar la venta: " + ventaResponse);
                                    runOnUiThread(() -> Toast.makeText(this, "Error al guardar la venta", Toast.LENGTH_SHORT).show());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error en la solicitud de guardar venta", e);
                                runOnUiThread(() -> Toast.makeText(this, "Error en la solicitud de guardar venta", Toast.LENGTH_SHORT).show());
                            }
                        } else {
                            Log.e(TAG, "Error al obtener dni_lec: " + result.getString("msg"));
                            runOnUiThread(() -> Toast.makeText(this, "Error al obtener dni_lec", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Log.e(TAG, "Solicitud fallida: " + response);
                        runOnUiThread(() -> Toast.makeText(this, "Error en la solicitud para obtener dni_lec", Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error en la solicitud para obtener dni_lec", e);
                    runOnUiThread(() -> Toast.makeText(this, "Error en la solicitud para obtener dni_lec", Toast.LENGTH_SHORT).show());
                }
            }).start();
        }
    }

    // Método para calcular el total en céntimos
    private int calcularTotalEnCentimos() {
        double total = 0.0;
        for (Libro libro : bookList) {
            total += libro.getPrecio_lib();
        }
        return (int) (total);  // Convertir a céntimos
    }

    private void enviarCorreoConPdf(String emailUser, JSONArray librosComprados) {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        // Crear el cliente HTTP para hacer la solicitud al servidor que generará el PDF
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", emailUser);
            jsonBody.put("items", librosComprados);
            jsonBody.put("total", calcularTotalEnCentimos());
            Log.d(TAG, "Datos a enviar: " + jsonBody);

        } catch (JSONException e) {
            Log.e(TAG, "Error creando el JSON para el envío del correo", e);
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://grupo1damb.pythonanywhere.com/complete_purchase")
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Correo enviado exitosamente con el PDF adjunto");
                } else {
                    Log.e(TAG, "Error al enviar el correo con el PDF: " + response);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error en la solicitud para enviar el correo con el PDF", e);
            }
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
