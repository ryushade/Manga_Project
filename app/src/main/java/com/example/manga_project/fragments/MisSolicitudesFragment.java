package com.example.manga_project.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.SoliHistorietaProveedorRequest;
import com.example.manga_project.Modelos.SoliHistorietaProveedorResponse;
import com.example.manga_project.R;
import com.example.manga_project.adapters.MisSolicitudesAdapter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisSolicitudesFragment extends Fragment {

    private static final String TAG = "MisSolicitudesFrag";

    private MisSolicitudesAdapter adapter;
    private List<SoliHistorietaProveedorRequest> data = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvSolicitudesEnviadas;
    private RecyclerView rvSolicitudes;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(
                R.layout.fragment_mis_solicitudes_historieta,
                container,
                false);

        swipeRefresh = v.findViewById(R.id.swipeRefresh);
        rvSolicitudes = v.findViewById(R.id.recyclerMisSolicitudes);
        rvSolicitudes.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MisSolicitudesAdapter(getContext(), data);
        rvSolicitudes.setAdapter(adapter);

        tvSolicitudesEnviadas = v.findViewById(R.id.tvSolicitudesEnviadas);
        tvSolicitudesEnviadas.setVisibility(View.GONE);

        swipeRefresh.setOnRefreshListener(this::cargarSolicitudes);
        cargarSolicitudes();

        return v;
    }

    private void cargarSolicitudes() {
        // configurar ApiClient para backend local
        ApiClient.setContext(getContext());
        // ApiClient.usarBackendLocal(true); // Ya no se usa, siempre es remoto
        swipeRefresh.setRefreshing(true);

        AuthService api = ApiClient.getClientConToken()
                .create(AuthService.class);

        api.obtenerMisSolicitudes().enqueue(new Callback<SoliHistorietaProveedorResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<SoliHistorietaProveedorResponse> call,
                    @NonNull Response<SoliHistorietaProveedorResponse> resp) {

                swipeRefresh.setRefreshing(false);

                if (!resp.isSuccessful() || resp.body() == null || !resp.body().isSuccess()) {
                    Toast.makeText(getContext(),
                            "Error servidor o sin datos",
                            Toast.LENGTH_SHORT).show();
                    mostrarEmpty();
                    return;
                }

                // DEBUG: ver JSON completo
                Log.d(TAG, "Resp body: " + new Gson().toJson(resp.body()));
                // DEBUG: ver lista de datos
                List<SoliHistorietaProveedorRequest> lista = resp.body().getData();
                Log.d(TAG, "Lista datos size=" + (lista != null ? lista.size() : 0)
                        + ", contenido=" + new Gson().toJson(lista));

                // procesar lista
                data.clear();
                if (lista != null && !lista.isEmpty()) {
                    data.addAll(lista);
                    tvSolicitudesEnviadas.setVisibility(View.GONE);
                    rvSolicitudes.setVisibility(View.VISIBLE);
                } else {
                    mostrarEmpty();
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(
                    @NonNull Call<SoliHistorietaProveedorResponse> call,
                    @NonNull Throwable t) {
                swipeRefresh.setRefreshing(false);
                Log.e(TAG, "Error de red", t);
                mostrarEmpty();
            }
        });
    }

    private void mostrarEmpty() {
        data.clear();
        adapter.notifyDataSetChanged();
        rvSolicitudes.setVisibility(View.GONE);
        tvSolicitudesEnviadas.setText(
                getString(R.string.mis_solicitudes_empty));
        tvSolicitudesEnviadas.setVisibility(View.VISIBLE);
    }
}
