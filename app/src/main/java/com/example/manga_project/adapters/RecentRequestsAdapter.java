package com.example.manga_project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.R;
import com.example.manga_project.model.AdminDashboardResponse;
import java.util.List;

public class RecentRequestsAdapter extends RecyclerView.Adapter<RecentRequestsAdapter.ViewHolder> {
    private List<AdminDashboardResponse.Reciente> recientes;

    public RecentRequestsAdapter(List<AdminDashboardResponse.Reciente> recientes) {
        this.recientes = recientes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminDashboardResponse.Reciente item = recientes.get(position);
        holder.tvTitle.setText(item.titulo);
        holder.tvDate.setText(item.fecha_solicitud);
        holder.tvEmail.setText(item.email);
    }

    @Override
    public int getItemCount() {
        return recientes != null ? recientes.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvEmail;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRecentTitle);
            tvDate = itemView.findViewById(R.id.tvRecentDate);
            tvEmail = itemView.findViewById(R.id.tvRecentEmail);
        }
    }
}

