package com.example.manga_project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.Modelos.VolumenResponse;
import com.example.manga_project.R;
import com.squareup.picasso.Picasso;
import java.util.List;

public class VolumenAdapter extends RecyclerView.Adapter<VolumenAdapter.VolumenViewHolder> {
    private List<VolumenResponse> volumenList;
    private Context context;

    public VolumenAdapter(List<VolumenResponse> volumenList, Context context) {
        this.volumenList = volumenList;
        this.context = context;
    }

    public VolumenAdapter(List<VolumenResponse> volumenList) {
        this.volumenList = volumenList;
        this.context = null;
    }

    @NonNull
    @Override
    public VolumenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_image, parent, false);
        return new VolumenViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VolumenViewHolder holder, int position) {
        VolumenResponse volumen = volumenList.get(position);
        holder.tvTitulo.setText(volumen.titulo);
        Picasso.get().load(volumen.portada).placeholder(R.drawable.ic_placeholder_portada).into(holder.ivPortada);
        holder.itemView.setOnClickListener(v -> {
            Context safeContext = (context != null) ? context : holder.itemView.getContext();
            android.content.Intent intent = new android.content.Intent(safeContext, com.example.manga_project.activities.HistorietaActivity.class);
            intent.putExtra("ID_VOLUMEN", volumen.id_volumen);
            safeContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return volumenList.size();
    }

    public void setVolumenes(List<VolumenResponse> volumenList) {
        this.volumenList = volumenList;
        notifyDataSetChanged();
    }

    static class VolumenViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo;
        ImageView ivPortada;
        VolumenViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.textViewName);
            ivPortada = itemView.findViewById(R.id.imageView);
        }
    }
}
