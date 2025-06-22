package com.example.manga_project.fragments;

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

public class CompradosFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comprados, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rvComprados);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        ItemUsuarioAdapter adapter = new ItemUsuarioAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(item -> {
            if (getContext() != null) {
                android.content.Intent intent = new android.content.Intent(getContext(), com.example.manga_project.activities.HistorietaActivity.class);
                intent.putExtra("ID_VOLUMEN", item.id_volumen);
                startActivity(intent);
            }
        });
        cargarComprados(adapter);
        return view;
    }

    private void cargarComprados(ItemUsuarioAdapter adapter) {
        AuthService api = ApiClient.getClientConToken().create(AuthService.class);
        api.getItemsUsuario("purchases").enqueue(new Callback<ItemsUsuarioResponse>() {
            @Override
            public void onResponse(Call<ItemsUsuarioResponse> call, Response<ItemsUsuarioResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    adapter.setItems(response.body().data, false);
                }
            }
            @Override public void onFailure(Call<ItemsUsuarioResponse> call, Throwable t) {
                // error
            }
        });
    }
}
