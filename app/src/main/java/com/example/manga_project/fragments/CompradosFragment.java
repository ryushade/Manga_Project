package com.example.manga_project.fragments;

import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.R;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.adapters.ItemUsuarioAdapter;
import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Modelos.ItemsUsuarioResponse;
import com.example.manga_project.Modelos.DevolucionRequest;
import com.example.manga_project.Modelos.DevolucionResponse;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompradosFragment extends Fragment {

    private ItemUsuarioAdapter adapter;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comprados, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rvComprados);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new ItemUsuarioAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(item -> {
            if (getContext() != null) {
                android.content.Intent intent = new android.content.Intent(getContext(), com.example.manga_project.activities.HistorietaActivity.class);
                intent.putExtra("ID_VOLUMEN", item.id_volumen);
                startActivity(intent);
            }
        });

        // Agregar listener para el botón de devolución
        adapter.setOnRefundClickListener(this::mostrarDialogoDevolucion);

        cargarComprados();
        return view;
    }

    private void cargarComprados() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        AuthService api = ApiClient.getClientConToken().create(AuthService.class);
        api.getItemsUsuario("purchases").enqueue(new Callback<ItemsUsuarioResponse>() {
            @Override
            public void onResponse(Call<ItemsUsuarioResponse> call, Response<ItemsUsuarioResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                // Log detallado del response para debugging
                android.util.Log.d("COMPRADOS", "Response code: " + response.code());
                android.util.Log.d("COMPRADOS", "Response URL: " + call.request().url());

                if (response.errorBody() != null) {
                    try {
                        String errorBody = response.errorBody().string();
                        android.util.Log.e("COMPRADOS", "Error body: " + errorBody);
                    } catch (Exception e) {
                        android.util.Log.e("COMPRADOS", "Error reading error body", e);
                    }
                }

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    android.util.Log.d("COMPRADOS", "Items cargados exitosamente: " + response.body().data.size());
                    adapter.setItems(response.body().data, false); // false porque NO es wishlist, son compras
                } else {
                    String errorMsg = "Error al cargar las compras";

                    // Manejo específico de errores
                    if (response.code() == 500) {
                        errorMsg = "Error interno del servidor al cargar las compras.\nInténtalo de nuevo en unos momentos.";
                        android.util.Log.e("COMPRADOS", "Error 500 - Problema en el servidor");
                    } else if (response.code() == 401) {
                        errorMsg = "Sesión expirada. Por favor inicia sesión de nuevo.";
                        android.util.Log.e("COMPRADOS", "Error 401 - No autorizado");
                    } else if (response.code() == 403) {
                        errorMsg = "No tienes permisos para ver las compras.";
                        android.util.Log.e("COMPRADOS", "Error 403 - Prohibido");
                    } else {
                        android.util.Log.e("COMPRADOS", "Error desconocido. Code: " + response.code());
                        errorMsg += " (Código: " + response.code() + ")";
                    }

                    mostrarError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ItemsUsuarioResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                android.util.Log.e("COMPRADOS", "Network error al cargar compras", t);

                String errorMsg = "Error de conexión";
                if (t instanceof java.net.SocketTimeoutException) {
                    errorMsg = "Tiempo de espera agotado. Verifica tu conexión a internet.";
                } else if (t instanceof java.net.UnknownHostException) {
                    errorMsg = "No se puede conectar al servidor. Verifica tu conexión a internet.";
                }

                mostrarError(errorMsg);
            }
        });
    }

    private void mostrarDialogoDevolucion(com.example.manga_project.Modelos.ItemUsuario item) {
        if (getContext() == null) return;

        // VERIFICAR SI YA FUE DEVUELTO
        if (!verificarSiPuedeDevolver(item)) {
            return; // La función ya muestra el mensaje de error correspondiente
        }

        // Crear el EditText para el motivo
        EditText editTextMotivo = new EditText(getContext());
        editTextMotivo.setHint("Ingresa el motivo de la devolución...");
        editTextMotivo.setMinLines(3);
        editTextMotivo.setMaxLines(5);

        new MaterialAlertDialogBuilder(getContext())
            .setTitle("Solicitar devolución")
            .setMessage("¿Estás seguro de que quieres solicitar la devolución de \"" + item.titulo + "\"?")
            .setView(editTextMotivo)
            .setPositiveButton("Solicitar devolución", (dialog, which) -> {
                String motivo = editTextMotivo.getText().toString().trim();
                if (motivo.isEmpty()) {
                    Toast.makeText(getContext(), "Por favor ingresa un motivo", Toast.LENGTH_SHORT).show();
                    return;
                }
                confirmarDevolucion(item, motivo);
            })
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_menu_revert) // Usar ícono del sistema
            .show();
    }

    /**
     * Verifica si un producto puede ser devuelto
     * @param item El item a verificar
     * @return true si puede ser devuelto, false si no
     */
    private boolean verificarSiPuedeDevolver(com.example.manga_project.Modelos.ItemUsuario item) {
        // Verificación 1: Campo puedeDevolver del backend
        if (!item.puedeDevolver) {
            mostrarMensajeNoDevolvible(item, "Este producto no es elegible para devolución.");
            return false;
        }

        // Verificación 2: Ya fue devuelto exitosamente
        if ("succeeded".equals(item.estadoDevolucion)) {
            mostrarMensajeNoDevolvible(item, "Este producto ya fue devuelto exitosamente.");
            return false;
        }

        // Verificación 3: ID de venta válido
        int idVentaParaVerificar = item.id_venta > 0 ? item.id_venta : item.id;
        if (idVentaParaVerificar <= 0) {
            mostrarMensajeNoDevolvible(item, "Error: No se puede procesar la devolución. Datos de venta no válidos.");
            return false;
        }

        return true; // Todas las verificaciones pasaron
    }

    /**
     * Muestra un mensaje cuando un producto no puede ser devuelto
     */
    private void mostrarMensajeNoDevolvible(com.example.manga_project.Modelos.ItemUsuario item, String razon) {
        if (getContext() == null) return;

        new MaterialAlertDialogBuilder(getContext())
            .setTitle("❌ No se puede devolver")
            .setMessage("Producto: " + item.titulo + "\n\n" + razon + "\n\n" +
                       "Si crees que esto es un error, contacta a nuestro equipo de soporte.")
            .setPositiveButton("Entendido", null)
            .setNeutralButton("Contactar soporte", (dialog, which) -> {
                // Aquí podrías abrir un intent para enviar email o abrir chat de soporte
                Toast.makeText(getContext(), "Redirigiendo a soporte...", Toast.LENGTH_SHORT).show();
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void confirmarDevolucion(com.example.manga_project.Modelos.ItemUsuario item, String motivo) {
        if (getContext() == null) return;

        new MaterialAlertDialogBuilder(getContext())
            .setTitle("Confirmar devolución")
            .setMessage("Se procesará la devolución del pago. Esta acción no se puede deshacer.")
            .setPositiveButton("Confirmar", (dialog, which) -> procesarDevolucion(item, motivo))
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    private void procesarDevolucion(com.example.manga_project.Modelos.ItemUsuario item, String motivo) {
        if (getContext() == null) return;

        // Usar item.id como id_venta si id_venta no está disponible
        int idVentaParaDevolucion = item.id_venta > 0 ? item.id_venta : item.id;

        // Debug: Verificar todos los campos del item
        android.util.Log.d("DEVOLUCION", "=== DEBUG ITEM ===");
        android.util.Log.d("DEVOLUCION", "item.id: " + item.id);
        android.util.Log.d("DEVOLUCION", "item.id_volumen: " + item.id_volumen);
        android.util.Log.d("DEVOLUCION", "item.id_venta: " + item.id_venta);
        android.util.Log.d("DEVOLUCION", "idVentaParaDevolucion: " + idVentaParaDevolucion);
        android.util.Log.d("DEVOLUCION", "item.titulo: " + item.titulo);
        android.util.Log.d("DEVOLUCION", "motivo: " + motivo);

        // Verificar si id_venta es válido
        if (idVentaParaDevolucion <= 0) {
            android.util.Log.e("DEVOLUCION", "ERROR: id_venta no válido: " + idVentaParaDevolucion);
            mostrarError("Error: No se puede procesar la devolución. ID de venta no válido.");
            return;
        }

        // Mostrar loading
        AlertDialog loadingDialog = new MaterialAlertDialogBuilder(getContext())
            .setTitle("Procesando devolución...")
            .setMessage("Por favor espera mientras procesamos tu solicitud")
            .setCancelable(false)
            .create();
        loadingDialog.show();

        AuthService api = ApiClient.getClientConToken().create(AuthService.class);
        DevolucionRequest request = new DevolucionRequest(idVentaParaDevolucion, motivo);

        // Debug: Log para ver qué se está enviando
        android.util.Log.d("DEVOLUCION", "Creando DevolucionRequest con:");
        android.util.Log.d("DEVOLUCION", "- id_ven: " + idVentaParaDevolucion);
        android.util.Log.d("DEVOLUCION", "- motivo: " + motivo);

        api.solicitarDevolucion(request).enqueue(new Callback<DevolucionResponse>() {
            @Override
            public void onResponse(Call<DevolucionResponse> call, Response<DevolucionResponse> response) {
                loadingDialog.dismiss();

                android.util.Log.d("DEVOLUCION", "Response code: " + response.code());
                if (response.errorBody() != null) {
                    try {
                        String errorBody = response.errorBody().string();
                        android.util.Log.e("DEVOLUCION", "Error body: " + errorBody);
                    } catch (Exception e) {
                        android.util.Log.e("DEVOLUCION", "Error reading error body", e);
                    }
                }

                if (response.isSuccessful() && response.body() != null) {
                    DevolucionResponse devolucion = response.body();
                    mostrarExitoDevolucion(devolucion, item);
                    // Recargar la lista para actualizar el estado
                    cargarComprados();
                } else {
                    String errorMsg = "Error al procesar la devolución";

                    if (response.code() == 500) {
                        errorMsg = "Error interno del servidor. La venta podría no tener información de pago válida o no existe.";
                    } else if (response.code() == 400) {
                        // Manejar específicamente el error de venta no válida
                        if (response.body() != null && response.body().getMsg() != null) {
                            String backendMsg = response.body().getMsg();
                            if (backendMsg.contains("no válida") || backendMsg.contains("reembolsada")) {
                                errorMsg = "⚠️ Esta compra no se puede reembolsar.\n\n" +
                                          "Posibles motivos:\n" +
                                          "• Ya fue reembolsada anteriormente\n" +
                                          "• La compra no es válida para devolución\n" +
                                          "• Hay un problema con los datos de la venta\n\n" +
                                          "Si crees que esto es un error, contacta a soporte.";
                            } else {
                                errorMsg = "Datos de la solicitud incorrectos: " + backendMsg;
                            }
                        } else {
                            errorMsg = "Datos de la solicitud incorrectos.";
                        }
                    } else if (response.code() == 401) {
                        errorMsg = "No autorizado. Por favor inicia sesión de nuevo.";
                    } else if (response.code() == 403) {
                        errorMsg = "No tienes permisos para reembolsar esta compra.";
                    } else if (response.code() == 404) {
                        errorMsg = "La compra no fue encontrada en nuestros registros.";
                    }

                    if (response.code() != 400) {
                        errorMsg += " (Código: " + response.code() + ")";
                    }

                    mostrarError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<DevolucionResponse> call, Throwable t) {
                loadingDialog.dismiss();
                android.util.Log.e("DEVOLUCION", "Network error", t);
                mostrarError("Error de conexión. Inténtalo de nuevo.");
            }
        });
    }

    private void mostrarExitoDevolucion(DevolucionResponse devolucion, com.example.manga_project.Modelos.ItemUsuario item) {
        if (getContext() == null) return;

        // Crear mensaje más amigable y estructurado
        String estado = devolucion.getStatus();
        String estadoAmigable = "";
        String tiempoEstimado = "";

        // Convertir estado técnico a mensaje amigable
        switch (estado.toLowerCase()) {
            case "succeeded":
                estadoAmigable = "✅ Procesado exitosamente";
                tiempoEstimado = "Tu reembolso estará disponible en 5-10 días hábiles.";
                break;
            case "pending":
                estadoAmigable = "⏳ En proceso";
                tiempoEstimado = "Tu reembolso se está procesando y estará disponible pronto.";
                break;
            case "requires_action":
                estadoAmigable = "⚠️ Requiere verificación";
                tiempoEstimado = "Se requiere una acción adicional para completar el reembolso.";
                break;
            default:
                estadoAmigable = "📝 " + estado;
                tiempoEstimado = "Te notificaremos cuando esté completado.";
                break;
        }

        String mensaje = "¡Tu solicitud de devolución ha sido procesada!\n\n" +
                        "📖 Producto: " + item.titulo + "\n\n" +
                        "💳 Estado del reembolso: " + estadoAmigable + "\n\n" +
                        "⏰ " + tiempoEstimado + "\n\n" +
                        "💡 El dinero será devuelto al mismo método de pago que usaste para la compra.\n\n" +
                        "Referencia: #" + devolucion.getRefund_id().substring(devolucion.getRefund_id().length() - 8).toUpperCase();

        new MaterialAlertDialogBuilder(getContext())
            .setTitle("🎉 ¡Devolución completada!")
            .setMessage(mensaje)
            .setPositiveButton("Perfecto", null)
            .setIcon(android.R.drawable.ic_dialog_info)
            .show();

        Toast.makeText(getContext(), "✅ Devolución procesada para " + item.titulo, Toast.LENGTH_LONG).show();
    }

    private void mostrarError(String mensaje) {
        if (getContext() == null) return;

        new MaterialAlertDialogBuilder(getContext())
            .setTitle("Error")
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    /**
     * Convierte estados técnicos en mensajes amigables
     */
    private String obtenerEstadoAmigable(String estado) {
        if (estado == null) return "Desconocido";

        switch (estado.toLowerCase()) {
            case "succeeded":
                return "✅ Completado exitosamente";
            case "pending":
                return "⏳ En proceso";
            case "requires_action":
                return "⚠️ Requiere verificación";
            case "failed":
                return "❌ Falló";
            case "canceled":
                return "🚫 Cancelado";
            default:
                return "📝 " + estado;
        }
    }

    /**
     * Verifica en tiempo real con el servidor si un producto puede ser devuelto
     * Útil para casos donde necesitas verificación adicional del backend
     */
    private void verificarDevolucionEnServidor(com.example.manga_project.Modelos.ItemUsuario item,
                                               OnVerificacionDevolucionListener listener) {
        AuthService api = ApiClient.getClientConToken().create(AuthService.class);

        // Crear una llamada para verificar el estado de devolución
        // Nota: Necesitarías agregar este endpoint en tu AuthService
        // api.verificarEstadoDevolucion(item.id_venta).enqueue(new Callback<VerificacionDevolucionResponse>() {
        //     @Override
        //     public void onResponse(Call<VerificacionDevolucionResponse> call, Response<VerificacionDevolucionResponse> response) {
        //         if (response.isSuccessful() && response.body() != null) {
        //             listener.onVerificacionCompleta(response.body().puedeDevolver, response.body().razon);
        //         } else {
        //             listener.onVerificacionCompleta(true, null); // Default: permitir si no se puede verificar
        //         }
        //     }
        //
        //     @Override
        //     public void onFailure(Call<VerificacionDevolucionResponse> call, Throwable t) {
        //         listener.onVerificacionCompleta(true, null); // Default: permitir si hay error de red
        //     }
        // });

        // Por ahora, usa verificación local hasta que implementes el endpoint
        listener.onVerificacionCompleta(verificarSiPuedeDevolver(item), null);
    }

    /**
     * Interface para manejar el resultado de verificación de devolución
     */
    private interface OnVerificacionDevolucionListener {
        void onVerificacionCompleta(boolean puedeDevolver, String razon);
    }

    /**
     * Método alternativo que usa verificación en servidor (para casos críticos)
     */
    private void mostrarDialogoDevolucionConVerificacion(com.example.manga_project.Modelos.ItemUsuario item) {
        if (getContext() == null) return;

        // Mostrar loading mientras verifica
        AlertDialog loadingDialog = new MaterialAlertDialogBuilder(getContext())
            .setTitle("Verificando...")
            .setMessage("Verificando si este producto puede ser devuelto")
            .setCancelable(false)
            .create();
        loadingDialog.show();

        verificarDevolucionEnServidor(item, (puedeDevolver, razon) -> {
            loadingDialog.dismiss();

            if (puedeDevolver) {
                mostrarDialogoDevolucion(item); // Proceder normalmente
            } else {
                String mensajeError = razon != null ? razon : "Este producto no puede ser devuelto";
                mostrarMensajeNoDevolvible(item, mensajeError);
            }
        });
    }
}
