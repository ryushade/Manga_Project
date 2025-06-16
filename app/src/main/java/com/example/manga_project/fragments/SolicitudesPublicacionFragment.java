package com.example.manga_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manga_project.R;
import com.example.manga_project.adapters.PublicacionAdapter;
import com.example.manga_project.Modelos.PublicacionItem;

import java.util.ArrayList;
import java.util.List;

public class SolicitudesPublicacionFragment extends Fragment {

    private RecyclerView recyclerView;
    private PublicacionAdapter adapter;
    private List<PublicacionItem> listaPublicaciones;

    public SolicitudesPublicacionFragment() {
        // Constructor vacío obligatorio
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_solicitud_historieta_proveedor, container, false);

        recyclerView = view.findViewById(R.id.recyclerSolicitudesPublicacion);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Simulación de datos: luego puedes reemplazar con tu llamada Retrofit
        listaPublicaciones = cargarDatosDeEjemplo();

        adapter = new PublicacionAdapter(getContext(), listaPublicaciones);
        recyclerView.setAdapter(adapter);

        return view;
    }

    // Simulación de datos, reemplazar por datos reales desde backend
    private List<PublicacionItem> cargarDatosDeEjemplo() {
        List<PublicacionItem> lista = new ArrayList<>();
        lista.add(new PublicacionItem(
                "El Viaje de Ñofi",
                "Manga",
                "Marco Rioja",
                "marco@gmail.com",
                "28/05/2025",
                "https://example.com/portada1.jpg" // usa una URL real o placeholder
        ));
        lista.add(new PublicacionItem(
                "Aventura Estelar",
                "Cómic",
                "Ana Torres",
                "ana@gmail.com",
                "01/06/2025",
                "https://example.com/portada2.jpg"
        ));
        return lista;
    }
}
