package com.example.ebook_proyect.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ebook_proyect.Api_cliente.ApiResponse;
import com.example.ebook_proyect.Api_cliente.ApiResponse_L;
import com.example.ebook_proyect.Api_cliente.AuthService;
import com.example.ebook_proyect.Api_cliente.Libro;
import com.example.ebook_proyect.Book;
import com.example.ebook_proyect.R;
import com.example.ebook_proyect.activities.DescriptionBookActivity;
import com.example.ebook_proyect.adapters.LibroAdapter;
import com.example.ebook_proyect.databinding.FragmentLibraryBinding;
import com.example.ebook_proyect.Api_cliente.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding binding;
    private List<Book> savedBooks;
    private List<Libro> purchasedBooks = new ArrayList<>(); // Cambiar a List<Libro>
    private List<Libro> listaGuardados = new ArrayList<>();
    private LibroAdapter purchasedAdapter;
    private ImageView iconExpand1, iconExpand2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializa las listas de libros con precios
        savedBooks = new ArrayList<>();
        purchasedBooks = new ArrayList<>();
        cargarLibrosGuardados();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        iconExpand1 = binding.iconExpand1;
        iconExpand2 = binding.iconExpand2;


        // Configurar RecyclerView de libros guardados
        binding.recyclerView1.setLayoutManager(new GridLayoutManager(getContext(), 2));
        LibroAdapter savedAdapter = new LibroAdapter(listaGuardados, purchasedBooks, getContext(),
                (itemView, bookId) -> abrirDescripcionLibro(itemView, bookId), true); // `true` para activar eliminación
        binding.recyclerView1.setAdapter(savedAdapter);


        // Configurar RecyclerView de libros comprados
        binding.recyclerView2.setLayoutManager(new GridLayoutManager(getContext(), 2));
        purchasedAdapter = new LibroAdapter(purchasedBooks, purchasedBooks, getContext(),
                (itemView, bookId) -> abrirPreviewFragment(itemView, bookId), false); // `false` para la sección de comprados
        binding.recyclerView2.setAdapter(purchasedAdapter);

        binding.cardView1.setOnClickListener(v -> toggleVisibility(binding.expandableContent1, binding.iconExpand1));
        binding.cardView2.setOnClickListener(v -> toggleVisibility(binding.expandableContent2, binding.iconExpand2));
        cargarLibrosGuardados();
        cargarLibrosComprados();
        return view;
    }


    private void abrirDescripcionLibro(View view, String bookId) {
        // Crear el Intent para abrir DescriptionBookActivity
        Intent intent = new Intent(getContext(), DescriptionBookActivity.class);
        intent.putExtra("BOOK_ID", bookId);
        startActivity(intent);
    }


    private void abrirPreviewFragment(View view, String bookId) {
        Bundle bundle = new Bundle();
        bundle.putString("BOOK_ID", bookId);
        Navigation.findNavController(view).navigate(R.id.action_libraryFragment_to_previewFragment, bundle);
    }

    private void toggleVisibility(View view, ImageView icon) {
        if (view.getVisibility() == View.GONE) {
            view.setVisibility(View.VISIBLE);
            if (icon != null) {
                icon.setImageResource(R.drawable.ico_down); // Cambia a ícono de expandido
            }
        } else {
            view.setVisibility(View.GONE);
            if (icon != null) {
                icon.setImageResource(R.drawable.ico_right); // Cambia a ícono de colapsado
            }
        }
    }

    private void cargarLibrosGuardados() {
        SharedPreferences userPrefs = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String email = userPrefs.getString("email_user", "");

        if (email.isEmpty()) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("GuardadosPrefs", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("guardados_" + email, "[]");
        Type type = new TypeToken<List<Libro>>() {}.getType();
        listaGuardados = new Gson().fromJson(json, type);
    }

    private void cargarLibrosComprados() {
        SharedPreferences userPrefs = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String email = userPrefs.getString("email_user", "");
        boolean isGoogleUser = userPrefs.getBoolean("is_google_user", false);

        if (email.isEmpty()) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        purchasedBooks.clear(); // Limpiar la lista antes de cargar nuevos datos

        if (isGoogleUser) {
            // Cargar libros comprados desde SharedPreferences
            SharedPreferences purchasedPrefs = getActivity().getSharedPreferences("LibrosCompradosPrefs", Context.MODE_PRIVATE);
            String json = purchasedPrefs.getString("comprados_" + email, "[]");
            Type type = new TypeToken<List<Libro>>() {}.getType();
            List<Libro> libros = new Gson().fromJson(json, type);

            if (libros != null) {
                for (Libro libro : libros) {
                    if (!purchasedBooks.contains(libro)) { // Evitar duplicados
                        purchasedBooks.add(libro);
                    }
                }
            }

            purchasedAdapter.notifyDataSetChanged();
            Log.d("LibraryFragment", "Libros comprados cargados desde SharedPreferences");
        } else {
            // Lógica existente para cargar libros comprados desde la API
            AuthService authService = ApiClient.getClientConToken().create(AuthService.class);
            Call<ApiResponse> call = authService.obtenerLibrosComprados(email);

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Libro> librosComprados = response.body().getLibros();
                        if (librosComprados != null) {
                            for (Libro libro : librosComprados) {
                                if (!purchasedBooks.contains(libro)) { // Evitar duplicados
                                    purchasedBooks.add(libro);
                                }
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Error al obtener libros comprados", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}