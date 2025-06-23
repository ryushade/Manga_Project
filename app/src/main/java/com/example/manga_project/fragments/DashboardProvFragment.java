package com.example.manga_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.DashboardProveedorResponseV2;
import com.example.manga_project.R;
import com.example.manga_project.adapters.VolumenProveedorAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardProvFragment extends Fragment {
    private TextView tvCountActive, tvCountPending;
    private RecyclerView rvMyProducts;
    private AuthService api;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home_proveedor, container, false);
        tvCountActive = v.findViewById(R.id.tvCountActive);
        tvCountPending = v.findViewById(R.id.tvCountPending);
        rvMyProducts = v.findViewById(R.id.rvMyProducts);
        rvMyProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        api = ApiClient.getClientConToken().create(AuthService.class);
        cargarDashboard();
        return v;
    }

    private void cargarDashboard() {
        api.getDashboardProveedor().enqueue(new Callback<DashboardProveedorResponseV2>() {
            @Override
            public void onResponse(Call<DashboardProveedorResponseV2> call, Response<DashboardProveedorResponseV2> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardProveedorResponseV2 data = response.body();
                    tvCountActive.setText(String.valueOf(data.activas));
                    tvCountPending.setText(String.valueOf(data.pendientes));
                    rvMyProducts.setAdapter(new VolumenProveedorAdapter(data.volumenes));
                }
            }
            @Override
            public void onFailure(Call<DashboardProveedorResponseV2> call, Throwable t) {
                // Manejo de error simple
            }
        });
    }
}
