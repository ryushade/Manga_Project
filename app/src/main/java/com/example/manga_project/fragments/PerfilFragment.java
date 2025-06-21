package com.example.manga_project.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Logout;
import com.example.manga_project.Modelos.PerfilResponse;
import com.example.manga_project.R;
import com.example.manga_project.activities.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PerfilFragment extends Fragment {

    private TextView txtNombreUsuario, txtEmail, txtConvertirseProveedor;
    private PerfilResponse perfil;
    private boolean solicitudEnviada = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ApiClient.setContext(requireContext());

        // Inicializar vistas
        txtNombreUsuario        = view.findViewById(R.id.txtNombreUsuario);
        txtEmail                = view.findViewById(R.id.txtEmail);
        txtConvertirseProveedor = view.findViewById(R.id.txtConvertirseProveedor);
        TextView txtCerrarSesion       = view.findViewById(R.id.txtCerrarSesion);
        CardView btnConvertirseProveedor = view.findViewById(R.id.btnConvertirseProveedor);

        // Listener Cerrar sesión
        txtCerrarSesion.setOnClickListener(v -> {
            if (getActivity() instanceof Logout) {
                ((Logout) getActivity()).logout();
            } else {
                SharedPreferences prefs = requireContext()
                        .getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                prefs.edit().clear().apply();

                startActivity(new Intent(requireContext(), LoginActivity.class));
                requireActivity().finish();
            }
        });

        // Listener Convertirse en proveedor
        btnConvertirseProveedor.setOnClickListener(v -> {
            if (solicitudEnviada) {
                mostrarDialogoCancelarSolicitudProveedor();
            } else {
                mostrarDialogoSolicitudProveedor();
            }
        });

        // Cargar datos del perfil
        obtenerDatosPerfil();
    }

    private void obtenerDatosPerfil() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
            return;
        }

        Retrofit retrofit = ApiClient.getClientConToken();
        AuthService apiService = retrofit.create(AuthService.class);
        Call<PerfilResponse> call = apiService.getPerfil();

        call.enqueue(new Callback<PerfilResponse>() {
            @Override
            public void onResponse(Call<PerfilResponse> call,
                                   Response<PerfilResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    perfil = response.body();
                    txtNombreUsuario.setText(perfil.getNombre());
                    txtEmail.setText(perfil.getEmail());

                    boolean esProveedor = perfil.getId_rol() == 2 || perfil.getId_rol() == 3;
                    solicitudEnviada = perfil.isProveedor_solicitud();

                    if (esProveedor) {
                        txtConvertirseProveedor.setVisibility(View.GONE);
                    } else {
                        txtConvertirseProveedor.setVisibility(View.VISIBLE);
                        if (solicitudEnviada) {
                            txtConvertirseProveedor.setText("Cancelar solicitud");
                            txtConvertirseProveedor.setTextColor(
                                    getResources().getColor(R.color.colorAccent));
                        } else {
                            txtConvertirseProveedor.setText("Convertirse en proveedor");
                            txtConvertirseProveedor.setTextColor(
                                    getResources().getColor(R.color.colorPrimary));
                        }
                    }
                } else {
                    int code = response.code();
                    if (code == 401 || code == 403) {
                        cerrarSesionPorTokenInvalido();
                    } else {
                        txtNombreUsuario.setText("Error (" + code + ")");
                        txtEmail.setText("");
                        Toast.makeText(requireContext(),
                                "Error al cargar perfil: " + code, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<PerfilResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        "Error de conexión al cargar perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoSolicitudProveedor() {
        new AlertDialog .Builder(requireContext())
                .setTitle("Convertirse en proveedor")
                .setMessage("¿Deseas solicitar ser proveedor y subir tus propios mangas o cómics?")
                .setPositiveButton("Sí, solicitar", (dialog, which) -> enviarSolicitudProveedor())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void enviarSolicitudProveedor() {
        Retrofit retrofit = ApiClient.getClientConToken();
        AuthService apiService = retrofit.create(AuthService.class);
        Call<Void> call = apiService.solicitarProveedor();

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call,
                                   Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(),
                            "Solicitud enviada con éxito", Toast.LENGTH_SHORT).show();
                    // Actualiza el estado localmente
                    if (perfil != null) {
                        perfil.setProveedor_solicitud(true);
                    }
                    txtConvertirseProveedor.setText("Solicitud en revisión");
                    txtConvertirseProveedor.setTextColor(
                            getResources().getColor(R.color.colorAccent));
                    // Refresca el perfil desde el backend para asegurar consistencia
                    obtenerDatosPerfil();
                } else {
                    Toast.makeText(requireContext(),
                            "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        "Error de conexión al enviar solicitud", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoCancelarSolicitudProveedor() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancelar solicitud")
                .setMessage("¿Seguro que deseas cancelar tu solicitud de proveedor?")
                .setPositiveButton("Sí, cancelar", (dialog, which) -> cancelarSolicitudProveedor())
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelarSolicitudProveedor() {
        Retrofit retrofit = ApiClient.getClientConToken();
        AuthService apiService = retrofit.create(AuthService.class);
        Call<com.example.manga_project.Modelos.RespuestaGenerica> call = apiService.cancelarSolicitudProveedor();
        call.enqueue(new Callback<com.example.manga_project.Modelos.RespuestaGenerica>() {
            @Override
            public void onResponse(Call<com.example.manga_project.Modelos.RespuestaGenerica> call, Response<com.example.manga_project.Modelos.RespuestaGenerica> response) {
                if (response.isSuccessful() && response.body() != null && response.body().code == 0) {
                    Toast.makeText(requireContext(), "Solicitud cancelada", Toast.LENGTH_SHORT).show();
                    solicitudEnviada = false;
                    if (perfil != null) perfil.setProveedor_solicitud(false);
                    obtenerDatosPerfil();
                } else {
                    Toast.makeText(requireContext(), "No se pudo cancelar la solicitud", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<com.example.manga_project.Modelos.RespuestaGenerica> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error de red al cancelar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cerrarSesionPorTokenInvalido() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        startActivity(new Intent(requireContext(), LoginActivity.class));
        requireActivity().finish();
    }
}
