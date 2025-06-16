package com.example.manga_project.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.CapituloResponse;
import com.example.manga_project.Modelos.PaginaResponse;
import com.example.manga_project.Modelos.SolicitudDetalle;
import com.example.manga_project.activities.HistorietaReaderActivity;
import com.example.manga_project.adapters.CapituloAdapter;
import com.example.manga_project.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RevisionFragment extends Fragment {
    private TabLayout    tabLayout;
    private Group        detailsGroup;
    private FrameLayout  chapterListContainer;
    private TextView     tvChapterHeader;
    private RecyclerView rvChapters;
    private CapituloAdapter chapterAdapter;
    private AuthService  api;
    private int          idSolicitud;
    private TextView     tvTitle, tvAuthor, tvSynopsis, tvPrice;
    private ImageView     ivCover;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ver_contenido,
                container, false);
    }

    @Override public void onViewCreated(@NonNull View view,
                                        @Nullable Bundle saved) {
        super.onViewCreated(view, saved);

        tabLayout            = view.findViewById(R.id.tabLayout);
        detailsGroup         = view.findViewById(R.id.detailsGroup);
        chapterListContainer = view.findViewById(R.id.chapterListContainer);
        tvChapterHeader      = view.findViewById(R.id.tvChapterHeader);
        rvChapters           = view.findViewById(R.id.rvChapters);
        tvTitle              = view.findViewById(R.id.tvTitle);
        tvAuthor             = view.findViewById(R.id.tvAuthor);
        tvSynopsis           = view.findViewById(R.id.tvSynopsis);
        tvPrice              = view.findViewById(R.id.tvPrice);
        ivCover              = view.findViewById(R.id.ivCover);

        rvChapters.setLayoutManager(
                new LinearLayoutManager(getContext())
        );
        chapterAdapter = new CapituloAdapter(
                new ArrayList<>(),
                this::onChapterClicked
        );
        rvChapters.setAdapter(chapterAdapter);

        api = ApiClient
                .getClientConToken()
                .create(AuthService.class);

        if (getArguments() != null) {
            idSolicitud = getArguments()
                    .getInt("ID_SOLICITUD");
        }

        // Llamar a la función para cargar los detalles de la solicitud
        loadSolicitudDetails();

        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override public void onTabSelected(TabLayout.Tab tab) {
                        if (tab.getPosition() == 0) {
                            chapterListContainer.setVisibility(View.VISIBLE);
                            detailsGroup.setVisibility(View.GONE);
                            loadChapters();
                        } else {
                            chapterListContainer.setVisibility(View.GONE);
                            detailsGroup.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override public void onTabUnselected(TabLayout.Tab t) {}
                    @Override public void onTabReselected(TabLayout.Tab t) {}
                });
        TabLayout.Tab first = tabLayout.getTabAt(0);
        if (first != null) first.select();
    }

    private void loadSolicitudDetails() {
        // Llama al endpoint para obtener los detalles de la solicitud
        api.getSolicitudById(idSolicitud).enqueue(new Callback<SolicitudDetalle>() {
            @Override
            public void onResponse(Call<SolicitudDetalle> call, Response<SolicitudDetalle> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SolicitudDetalle solicitud = response.body();
                    tvTitle.setText("Título: " + solicitud.getTitulo());
                    tvAuthor.setText("Autor(es): " + solicitud.getAutores());
                    tvSynopsis.setText(solicitud.getDescripcion());
                    tvPrice.setText("$" + solicitud.getPrecio_volumen());
                    // Cargar imagen de portada si hay URL
                    if (solicitud.getUrl_portada() != null && !solicitud.getUrl_portada().isEmpty()) {
                        // Usa tu librería de imágenes preferida, por ejemplo Glide:
                        // Glide.with(getContext()).load(solicitud.getUrl_portada()).into(ivCover);
                    }
                } else {
                    Toast.makeText(getContext(), "No se pudo cargar la solicitud", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SolicitudDetalle> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red al cargar la solicitud", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChapters() {
        api.getChapters(idSolicitud)
                .enqueue(new Callback<CapituloResponse>() {
                    @Override public void onResponse(
                            Call<CapituloResponse> call,
                            Response<CapituloResponse> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().code == 0) {
                            List<String> chapters =
                                    response.body().chapters;
                            tvChapterHeader.setText(
                                    "Capítulos (" + chapters.size() + ")"
                            );
                            chapterAdapter.actualizarDatos(chapters);
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al cargar capítulos",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(
                            Call<CapituloResponse> call,
                            Throwable t
                    ) {
                        Toast.makeText(getContext(),
                                "Fallo de conexión",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onChapterClicked(String chapterName) {
        api.getChapterPages(idSolicitud, chapterName)
                .enqueue(new Callback<PaginaResponse>() {
                    @Override public void onResponse(
                            Call<PaginaResponse> call,
                            Response<PaginaResponse> response
                    ) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().code == 0) {
                            ArrayList<String> urls =
                                    new ArrayList<>(response.body().pages);
                            Intent i = new Intent(
                                    getContext(),
                                    HistorietaReaderActivity.class
                            );
                            i.putStringArrayListExtra("PAGES", urls);
                            startActivity(i);
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al cargar páginas",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(
                            Call<PaginaResponse> call,
                            Throwable t
                    ) {
                        Toast.makeText(getContext(),
                                "Fallo de conexión",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
