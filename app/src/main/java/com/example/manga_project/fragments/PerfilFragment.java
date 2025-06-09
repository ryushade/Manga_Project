package com.example.manga_project.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.PerfilResponse;
import com.example.manga_project.R;
import com.example.manga_project.activities.LoginActivity;
import com.example.manga_project.activities.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PerfilFragment extends Fragment {

    private TextView txtNombreUsuario, txtEmail, txtConvertirseProveedor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtNombreUsuario = view.findViewById(R.id.txtNombreUsuario);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtConvertirseProveedor = view.findViewById(R.id.txtConvertirseProveedor);

        ApiClient.setContext(requireContext());
        obtenerDatosPerfil();

        TextView txtCerrarSesion = view.findViewById(R.id.txtCerrarSesion);
        txtCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).logout();
                }
            }
        });

        LinearLayout btnConvertirseProveedor = view.findViewById(R.id.btnConvertirseProveedor);
        btnConvertirseProveedor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoSolicitudProveedor();
            }
        });
    }

    private void obtenerDatosPerfil() {
        SharedPreferences prefs = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
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
            public void onResponse(Call<PerfilResponse> call, Response<PerfilResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PerfilResponse perfil = response.body();
                    txtNombreUsuario.setText(perfil.getNombre());
                    txtEmail.setText(perfil.getEmail());

                    if (perfil.isProveedor_solicitud()) {
                        txtConvertirseProveedor.setText("Solicitud en revisión");
                        txtConvertirseProveedor.setTextColor(getResources().getColor(R.color.colorAccent)); // opcional
                    }
                } else {
                    int code = response.code();
                    if (code == 401 || code == 403) {
                        SharedPreferences prefs = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                        prefs.edit().remove("token").apply();
                        startActivity(new Intent(requireContext(), LoginActivity.class));
                        requireActivity().finish();
                    } else {
                        txtNombreUsuario.setText("Error (" + code + ")");
                        txtEmail.setText("");
                    }
                }
            }

            @Override
            public void onFailure(Call<PerfilResponse> call, Throwable t) {
                txtNombreUsuario.setText("Error de conexión");
                txtEmail.setText("");
            }
        });
    }

    private void mostrarDialogoSolicitudProveedor() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Convertirse en proveedor")
                .setMessage("¿Deseas solicitar ser proveedor y subir tus propios mangas o cómics?")
                .setPositiveButton("Sí, solicitar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        enviarSolicitudProveedor();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void enviarSolicitudProveedor() {
        Retrofit retrofit = ApiClient.getClientConToken();
        AuthService apiService = retrofit.create(AuthService.class);
        Call<Void> call = apiService.solicitarProveedor();

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Solicitud enviada con éxito", Toast.LENGTH_SHORT).show();
                    txtConvertirseProveedor.setText("Solicitud en revisión");
                    txtConvertirseProveedor.setTextColor(getResources().getColor(R.color.colorAccent)); // opcional
                } else {
                    Toast.makeText(requireContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
