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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MisSolicitudesFragment extends Fragment {

    private static final String TAG = "MisSolicitudesFrag";

    private MisSolicitudesAdapter adapter;
    private List<SoliHistorietaProveedorRequest> data = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvSolicitudesEnviadas;

    public MisSolicitudesFragment() { }

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
        RecyclerView rv = v.findViewById(R.id.recyclerMisSolicitudes);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MisSolicitudesAdapter(getContext(), data);
        rv.setAdapter(adapter);

        tvSolicitudesEnviadas = v.findViewById(R.id.tvSolicitudesEnviadas);
        tvSolicitudesEnviadas.setVisibility(View.GONE);

        swipeRefresh.setOnRefreshListener(this::cargarSolicitudes);

        // Carga inicial
        cargarSolicitudes();

        return v;
    }

    private void cargarSolicitudes() {
        swipeRefresh.setRefreshing(true);
        Retrofit retrofit = ApiClient.getClientConToken();
        AuthService api = retrofit.create(AuthService.class);

        api.obtenerMisSolicitudes().enqueue(new Callback<SoliHistorietaProveedorResponse>() {
            @Override
            public void onResponse(@NonNull Call<SoliHistorietaProveedorResponse> call,
                                   @NonNull Response<SoliHistorietaProveedorResponse> resp) {
                swipeRefresh.setRefreshing(false);

                if (!resp.isSuccessful()) {
                    Toast.makeText(getContext(), "Error servidor", Toast.LENGTH_SHORT).show();
                    return;
                }

                SoliHistorietaProveedorResponse body = resp.body();
                if (body == null) {
                    Toast.makeText(getContext(), "Sin datos", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<SoliHistorietaProveedorRequest> lista = body.getData();
                data.clear();
                if (lista != null && !lista.isEmpty()) {
                    data.addAll(lista);
                    tvSolicitudesEnviadas.setVisibility(View.GONE);
                } else {
                    tvSolicitudesEnviadas.setText("No tienes solicitudes enviadas");
                    tvSolicitudesEnviadas.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }


            @Override
            public void onFailure(@NonNull Call<SoliHistorietaProveedorResponse> call,
                                  @NonNull Throwable t) {
                swipeRefresh.setRefreshing(false);
                Log.e(TAG, "Fallo red: " + t.getMessage());
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
