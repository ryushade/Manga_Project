package com.example.manga_project.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    private TextView txtNombreUsuario, txtEmail;

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

        ApiClient.setContext(requireContext()); // Para obtener SharedPreferences en el Interceptor
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
    }

    private void obtenerDatosPerfil() {
        // Pre-chequeo opcional de token, pero el Interceptor lo usará de todos modos
        SharedPreferences prefs = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            // Si no hay token, fuerza logout
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
            return;
        }

        Retrofit retrofit = ApiClient.getClientConToken();
        AuthService apiService = retrofit.create(AuthService.class);
        Call<PerfilResponse> call = apiService.getPerfil(); // No pasas token aquí, lo añade el Interceptor

        call.enqueue(new Callback<PerfilResponse>() {
            @Override
            public void onResponse(Call<PerfilResponse> call, Response<PerfilResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PerfilResponse perfil = response.body();
                    txtNombreUsuario.setText(perfil.getNombre());
                    txtEmail.setText(perfil.getEmail());
                } else {
                    int code = response.code();
                    if (code == 401 || code == 403) {
                        // Token expirado/inválido: limpia sesión y redirige a login
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
}
