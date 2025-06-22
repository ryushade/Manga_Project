package com.example.manga_project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.Modelos.Comentario;
import com.example.manga_project.R;
import java.util.ArrayList;
import java.util.List;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder> {
    private List<Comentario> comentarios;

    public ComentarioAdapter(List<Comentario> comentarios) {
        if (comentarios == null) {
            this.comentarios = new ArrayList<>();
        } else {
            this.comentarios = comentarios;
        }
    }

    @NonNull
    @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ComentarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        Comentario comentario = comentarios.get(position);
        holder.tvUserName.setText(comentario.usuario);
        holder.tvCommentDate.setText(comentario.fecha);
        holder.tvCommentText.setText(comentario.texto);
        // Avatar fijo, puedes personalizar si tienes imágenes
        holder.imgUserAvatar.setImageResource(R.drawable.ic_person);
    }

    @Override
    public int getItemCount() {
        return comentarios != null ? comentarios.size() : 0;
    }

    public void agregarComentario(Comentario comentario) {
        comentarios.add(0, comentario);
        notifyItemInserted(0);
    }

    public void setComentarios(List<Comentario> comentarios) {
        if (comentarios == null) {
            this.comentarios = new ArrayList<>();
        } else {
            this.comentarios = comentarios;
        }
        notifyDataSetChanged();
    }

    public static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        ImageView imgUserAvatar;
        TextView tvUserName, tvCommentDate, tvCommentText;
        public ComentarioViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvCommentDate = itemView.findViewById(R.id.tvCommentDate);
            tvCommentText = itemView.findViewById(R.id.tvCommentText);
        }
    }
}
