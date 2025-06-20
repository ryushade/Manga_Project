package com.example.manga_project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.manga_project.R;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<Libro> bookList;
    private final OnRemoveClickListener removeClickListener;

    // Interfaz para manejar los clics en el botón de eliminar
    public interface OnRemoveClickListener {
        void onRemoveClick(Libro book);
    }

    // Constructor
    public CartAdapter(List<Libro> bookList, OnRemoveClickListener removeClickListener) {
        this.bookList = bookList;
        this.removeClickListener = removeClickListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Libro book = bookList.get(position);

        // Configura los datos en las vistas
        holder.titleTextView.setText(book.getTitulo_lib());
        holder.authorTextView.setText(book.getNombre_completo());
        holder.precioTextView.setText ("S/. " +book.getPrecio_lib());
        //holder.descriptionTextView.setText(book.getDescripcion());

        // Configura la descripción para que sea desplazable
        //holder.descriptionTextView.setMovementMethod(new ScrollingMovementMethod());

        // Cargar la imagen con Glide
        Glide.with(holder.itemView.getContext())
                .load(book.getImage())
                .into(holder.imageView);

        // Configura el botón de eliminar
        holder.deleteButton.setOnClickListener(v -> removeClickListener.onRemoveClick(book));
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    // Clase ViewHolder
    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView authorTextView;
        TextView precioTextView;
        //TextView descriptionTextView;
        Button deleteButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            precioTextView = itemView.findViewById(R.id.precioTextView);
            //descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
