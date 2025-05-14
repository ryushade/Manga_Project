package com.example.manga_project.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.manga_project.Api_cliente.Libro;
import com.example.manga_project.R;
import com.example.manga_project.activities.DescriptionBookActivity;

import java.util.List;

public class SearchBookAdapter extends RecyclerView.Adapter<SearchBookAdapter.SearchBookViewHolder> {
    private List<Libro> libros;
    private Context context;

    // Constructor que acepta la lista de libros y el contexto
    public SearchBookAdapter(List<Libro> libros, Context context) {
        this.libros = libros;
        this.context = context;
    }

    // Método para actualizar la lista de libros
    public void setBooks(List<Libro> nuevosLibros) {
        this.libros = nuevosLibros;
        notifyDataSetChanged(); // Notifica que los datos han cambiado
    }

    @NonNull
    @Override
    public SearchBookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new SearchBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchBookViewHolder holder, int position) {
        Libro libro = libros.get(position);

        // Cargar la imagen usando Glide
        Glide.with(holder.itemView.getContext())
                .load(libro.getImage())
                .into(holder.imageView);

        // Configurar el título, autor y descripción
        holder.titleTextView.setText(libro.getTitulo_lib());
        holder.authorTextView.setText(libro.getNombre_completo());
        holder.descriptionTextView.setText(libro.getDescripcion());

        // Configurar el clic para abrir DescriptionBookActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DescriptionBookActivity.class);
            intent.putExtra("BOOK_ID", libro.getIsbn_lib());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return libros.size();
    }

    static class SearchBookViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView authorTextView;
        TextView descriptionTextView;

        public SearchBookViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            titleTextView = itemView.findViewById(R.id.textViewTitle);
            authorTextView = itemView.findViewById(R.id.textViewAuthor);
            descriptionTextView = itemView.findViewById(R.id.textViewDescription);
        }
    }
}
