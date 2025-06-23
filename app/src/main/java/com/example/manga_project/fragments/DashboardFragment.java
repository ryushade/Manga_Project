package com.example.manga_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.R;
import com.example.manga_project.adapters.RecentRequestsAdapter;
import com.example.manga_project.model.AdminDashboardResponse;

import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import androidx.core.content.ContextCompat;

public class DashboardFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_admin, container, false);

        TextView tvCountProviders = view.findViewById(R.id.tvCountProviders);
        TextView tvCountPublications = view.findViewById(R.id.tvCountPublications);
        TextView lblUserName = view.findViewById(R.id.lblUserName);
        TextView lblDate = view.findViewById(R.id.lblDate);
        RecyclerView rvRecentRequests = view.findViewById(R.id.rvRecentRequests);

        // Mostrar fecha actual
        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        lblDate.setText(fechaHoy);

        // Puedes personalizar el nombre de usuario aquí si lo tienes guardado
        lblUserName.setText(R.string.admin_dashboard_username);

        AuthService authService = ApiClient.getClientConToken().create(AuthService.class);
        authService.getAdminDashboard().enqueue(new Callback<AdminDashboardResponse>() {
            @Override
            public void onResponse(Call<AdminDashboardResponse> call, Response<AdminDashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AdminDashboardResponse data = response.body();
                    tvCountProviders.setText(String.valueOf(data.stats.proveedores_pendientes));
                    tvCountPublications.setText(String.valueOf(data.stats.publicaciones_pendientes));

                    // Lista de recientes
                    RecentRequestsAdapter adapter = new RecentRequestsAdapter(data.recientes);
                    rvRecentRequests.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<AdminDashboardResponse> call, Throwable t) {
                // Manejar error
            }
        });
        return view;
    }
}
