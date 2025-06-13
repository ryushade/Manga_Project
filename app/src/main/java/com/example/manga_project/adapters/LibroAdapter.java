package com.example.manga_project.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.manga_project.Api_cliente.Libro;
import com.example.manga_project.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

import java.util.List;

public class LibroAdapter extends RecyclerView.Adapter<LibroAdapter.LibroViewHolder> {
    private List<Libro> libros;
    private List<Libro> purchasedBooks; // Libros comprados
    private Context context;
    private OnItemClickListener onItemClickListener;
    private boolean isSavedSection; // Nueva variable para distinguir entre secciones

    // Constructor actualizado
    public LibroAdapter(List<Libro> libros, List<Libro> purchasedBooks, Context context, OnItemClickListener onItemClickListener, boolean isSavedSection) {
        this.libros = libros;
        this.purchasedBooks = purchasedBooks;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
        this.isSavedSection = isSavedSection; // Inicializar la variable
    }

    @NonNull
    @Override
    public LibroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book_image, parent, false);
        return new LibroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LibroViewHolder holder, int position) {
        Libro libro = libros.get(position);

        // Cargar la imagen del libro
        Glide.with(context)
                .load(libro.getImage())
                .placeholder(R.drawable.logo_ebook)
                .into(holder.imageView);

        // Configurar textos
        holder.textViewName.setText(libro.getTitulo_lib());
        holder.textViewPrice.setText(String.format("S/. %.2f", libro.getPrecio_lib()));

        // Verificar si el libro está comprado
        boolean isPurchased = isBookPurchased(libro.getIsbn_lib());

        if (isPurchased) {
            // Ocultar el botón si está en la sección de libros comprados
            holder.saveButton.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v ->
                    onItemClickListener.onItemClick(v, libro.getIsbn_lib()) // Abrir descripción
            );
        } else {
            // Mostrar botón y manejar clic según la sección
            holder.saveButton.setVisibility(View.VISIBLE);

            if (isSavedSection) {
                // En la sección de guardados, el botón elimina el libro
                holder.saveButton.setText("Eliminar");
                holder.saveButton.setOnClickListener(v -> eliminarLibroGuardado(libro, position));
            } else {
                // En otras secciones, el botón guarda el libro
                holder.saveButton.setText("Guardar");
                holder.saveButton.setOnClickListener(v -> guardarLibro(libro));
            }

            // Permitir clic en el resto del libro para abrir la descripción
            holder.itemView.setOnClickListener(v ->
                    onItemClickListener.onItemClick(v, libro.getIsbn_lib())
            );
        }
    }


    private void eliminarLibroGuardado(Libro libro, int position) {
        // Obtener el correo del usuario desde SharedPreferences
        SharedPreferences userPrefs = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String email = userPrefs.getString("email_user", "");

        if (email.isEmpty()) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener SharedPreferences para los libros guardados
        SharedPreferences sharedPreferences = context.getSharedPreferences("GuardadosPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Obtener la lista actual de libros guardados
        Gson gson = new Gson();
        String json = sharedPreferences.getString("guardados_" + email, "[]");
        Type type = new TypeToken<List<Libro>>() {}.getType();
        List<Libro> guardados = gson.fromJson(json, type);

        // Eliminar el libro de la lista de guardados
        if (guardados.removeIf(guardado -> guardado.getIsbn_lib().equals(libro.getIsbn_lib()))) {
            Toast.makeText(context, "Libro eliminado de guardados", Toast.LENGTH_SHORT).show();

            // Guardar la lista actualizada en SharedPreferences
            String guardadosActualizado = gson.toJson(guardados);
            editor.putString("guardados_" + email, guardadosActualizado);
            editor.apply();

            // Eliminar el libro de la lista actual y actualizar la vista
            libros.remove(position);
            notifyItemRemoved(position);
        } else {
            Toast.makeText(context, "Error al eliminar el libro", Toast.LENGTH_SHORT).show();
        }
    }




    private boolean isBookPurchased(String bookId) {
        for (Libro libro : purchasedBooks) {
            if (libro.getIsbn_lib().equals(bookId)) {
                return true;
            }
        }
        return false;
    }

    private void guardarLibro(Libro libro) {
        // Obtener el correo del usuario desde SharedPreferences
        SharedPreferences userPrefs = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String email = userPrefs.getString("email_user", "");

        if (email.isEmpty()) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener SharedPreferences para los libros guardados del usuario
        SharedPreferences sharedPreferences = context.getSharedPreferences("GuardadosPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Obtener la lista actual de libros guardados para el usuario específico
        Gson gson = new Gson();
        String json = sharedPreferences.getString("guardados_" + email, "[]");
        Type type = new TypeToken<List<Libro>>() {}.getType();
        List<Libro> guardados = gson.fromJson(json, type);

        // Verificar si el libro ya está guardado
        boolean libroYaGuardado = false;
        for (Libro guardado : guardados) {
            if (guardado.getIsbn_lib().equals(libro.getIsbn_lib())) {
                libroYaGuardado = true;
                break;
            }
        }

        if (libroYaGuardado) {
            Toast.makeText(context, "El libro ya está guardado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Agregar el libro a los guardados y guardar los datos actualizados para el usuario específico
        guardados.add(libro);
        String guardadosActualizado = gson.toJson(guardados);
        editor.putString("guardados_" + email, guardadosActualizado);
        editor.apply();

        Toast.makeText(context, "Libro guardado", Toast.LENGTH_SHORT).show();
    }


    @Override
    public int getItemCount() {
        return libros.size();
    }

    public static class LibroViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imageView;
        TextView textViewName;
        TextView textViewPrice;
        MaterialButton saveButton;

        public LibroViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textViewName = itemView.findViewById(R.id.textViewName);

        }
    }

    // Interfaz para manejar los clics
    public interface OnItemClickListener {
        void onItemClick(View view, String bookId); // Acepta el View y el ID del libro
    }
}