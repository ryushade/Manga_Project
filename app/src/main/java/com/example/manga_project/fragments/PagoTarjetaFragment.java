package com.example.manga_project.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Modelos.ListarCarritoResponse.ItemCarrito;
import com.example.manga_project.R;
import com.example.manga_project.activities.DetalleOrdenActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PagoTarjetaFragment extends Fragment {

    private TextInputEditText etNombre, etNumero, etVencimiento, etCvv;
    private MaterialButton btnConfirmar;
    private AuthService api;
    private SharedPreferences sharedPreferences;
    private List<ItemCarrito> carritoItems = new ArrayList<>();

    public static PagoTarjetaFragment newInstance(ArrayList<ItemCarrito> items) {
        PagoTarjetaFragment fragment = new PagoTarjetaFragment();
        Bundle args = new Bundle();
        args.putSerializable("carrito", items);
        fragment.setArguments(args);
        return fragment;
    }

    public PagoTarjetaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("carrito")) {
            carritoItems = (ArrayList<ItemCarrito>) getArguments().getSerializable("carrito");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pago_tarjeta, container, false);
        etNombre = view.findViewById(R.id.etNombre);
        etNumero = view.findViewById(R.id.etNumero);
        etVencimiento = view.findViewById(R.id.etVencimiento);
        etCvv = view.findViewById(R.id.etCvv);
        btnConfirmar = view.findViewById(R.id.btnConfirmarPago);
        sharedPreferences = requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        api = ApiClient.getClientConToken().create(AuthService.class);
        btnConfirmar.setOnClickListener(v -> validarDatosStripe());
        return view;
    }

    private void validarDatosStripe() {
        String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";
        String numero = etNumero.getText() != null ? etNumero.getText().toString().trim() : "";
        String vencimiento = etVencimiento.getText() != null ? etVencimiento.getText().toString().trim() : "";
        String cvv = etCvv.getText() != null ? etCvv.getText().toString().trim() : "";
        if (nombre.isEmpty() || numero.isEmpty() || vencimiento.isEmpty() || cvv.isEmpty()) {
            Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
        // Aquí deberías iniciar el flujo de pago con Stripe usando los datos ingresados
        Toast.makeText(getContext(), "Implementa aquí el flujo de pago con Stripe", Toast.LENGTH_SHORT).show();
    }
}
