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
import com.example.manga_project.Modelos.ItemUsuario;
import com.example.manga_project.Modelos.ApiResponse;
import com.example.manga_project.adapters.ItemUsuarioAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.List;

public class MisListasFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_listas, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rvMisListas);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        ItemUsuarioAdapter adapter = new ItemUsuarioAdapter();
        recyclerView.setAdapter(adapter);
        cargarMisListas(adapter);
        return view;
    }

    private void cargarMisListas(ItemUsuarioAdapter adapter) {
        // Cambia la URL base por la de tu backend
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://TU_BACKEND_URL/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AuthService api = retrofit.create(AuthService.class);
        int idUser = obtenerIdUsuario(); // Implementa este método según tu lógica
        api.getItemsUsuario(idUser, "wishlist").enqueue(new Callback<ApiResponse<List<ItemUsuario>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ItemUsuario>>> call, Response<ApiResponse<List<ItemUsuario>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    adapter.setItems(response.body().data, true); // true = es wishlist
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<ItemUsuario>>> call, Throwable t) {
                // Maneja el error (puedes mostrar un Toast, etc.)
            }
        });
    }

    private int obtenerIdUsuario() {
        // TODO: Implementa la obtención del id del usuario logueado
        return 1;
    }
}
