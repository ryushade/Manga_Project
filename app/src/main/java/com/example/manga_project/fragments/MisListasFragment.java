package com.example.manga_project.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisListasFragment extends Fragment {
    private ItemUsuarioAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mis_listas, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.rvMisListas);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new ItemUsuarioAdapter();
        recyclerView.setAdapter(adapter);
        cargarMisListas(adapter);
    }

    private void cargarMisListas(ItemUsuarioAdapter adapter) {
        AuthService api = ApiClient.getClientConToken().create(AuthService.class);
        int idUser = obtenerIdUsuario();
        android.util.Log.d("MisListasFragment", "userId=" + idUser);
        api.getItemsUsuario(idUser, "wishlist").enqueue(new Callback<ItemsUsuarioResponse>() {
            @Override
            public void onResponse(Call<ItemsUsuarioResponse> call, Response<ItemsUsuarioResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    adapter.setItems(response.body().data, true);
                }
            }
            @Override public void onFailure(Call<ItemsUsuarioResponse> call, Throwable t) {
                // error
            }
        });
    }

    private int obtenerIdUsuario() {
        SharedPreferences sp = requireContext().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        return sp.getInt("userId", -1);
    }
}
