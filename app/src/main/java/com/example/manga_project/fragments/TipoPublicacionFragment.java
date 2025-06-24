package com.example.manga_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.manga_project.R;

public class TipoPublicacionFragment extends Fragment {

    public TipoPublicacionFragment() {
        // Constructor vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_seleccionar_tipo_publi, container, false);

        View cardManga = view.findViewById(R.id.cardManga);
        View cardComic = view.findViewById(R.id.cardComic);

        // Oculta la opción de publicar manga
        cardManga.setVisibility(View.GONE);

        // Cambia el texto del cardComic a "Publicar historieta"
        TextView tvComic = view.findViewById(R.id.tvComic);
        if (tvComic != null) {
            tvComic.setText("Publicar historieta");
        }

        cardComic.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_tipoPublicacionFragment_to_publicarComicFragment);
        });

        return view;
    }
}
