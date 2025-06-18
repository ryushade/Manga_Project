package com.example.manga_project.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.manga_project.Modelos.AprobarPublicacionRequest;
import com.example.manga_project.Modelos.CapituloResponse;
import com.example.manga_project.Modelos.PaginaResponse;
import com.example.manga_project.Modelos.SolicitudDetalle;
import com.example.manga_project.Modelos.SolicitudResponse;
import com.example.manga_project.R;
import com.example.manga_project.activities.HistorietaReaderActivity;
import com.example.manga_project.adapters.CapituloAdapter;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

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

    // Detalles
    private TextView  tvTitle, tvAuthor, tvSynopsis, tvPrice;
    private ImageView ivCover;

    // Bloque de rechazo
    private EditText      etReason;
    private LinearLayout  llActions;
    private Button        btnApprove;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ver_contenido,
                container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ---- findViewById ----
        tabLayout            = view.findViewById(R.id.tabLayout);
        detailsGroup         = view.findViewById(R.id.detailsGroup);
        chapterListContainer = view.findViewById(R.id.chapterListContainer);
        tvChapterHeader      = view.findViewById(R.id.tvChapterHeader);
        rvChapters           = view.findViewById(R.id.rvChapters);

        tvTitle    = view.findViewById(R.id.tvTitle);
        tvAuthor   = view.findViewById(R.id.tvAuthor);
        tvSynopsis = view.findViewById(R.id.tvSynopsis);
        tvPrice    = view.findViewById(R.id.tvPrice);
        ivCover    = view.findViewById(R.id.ivCover);

        etReason   = view.findViewById(R.id.etRejectionReason);
        llActions  = view.findViewById(R.id.llActions);
        btnApprove = view.findViewById(R.id.btn_approve);

        // Ocultos al inicio
        etReason.setVisibility(View.GONE);
        llActions.setVisibility(View.GONE);

        // ---- RecyclerView ----
        rvChapters.setLayoutManager(new LinearLayoutManager(getContext()));
        chapterAdapter = new CapituloAdapter(new ArrayList<>(), this::onChapterClicked);
        rvChapters.setAdapter(chapterAdapter);

        // Listener de scroll para mostrar/ocultar el bloque inferior
        rvChapters.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                boolean alFin = !rv.canScrollVertically(1); // 1 → abajo
                if (alFin && etReason.getVisibility() == View.GONE) {
                    etReason.setVisibility(View.VISIBLE);
                    llActions.setVisibility(View.VISIBLE);
                } else if (!alFin && etReason.getVisibility() == View.VISIBLE) {
                    etReason.setVisibility(View.GONE);
                    llActions.setVisibility(View.GONE);
                }
            }
        });

        // ---- Retrofit ----
        api = ApiClient.getClientConToken().create(AuthService.class);

        if (getArguments() != null) {
            idSolicitud = getArguments().getInt("ID_SOLICITUD");
        }

        loadSolicitudDetails();   // detalles
        setupTabs();              // pestañas + capítulos

        btnApprove.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("¿Aprobar publicación?")
                .setMessage("¿Estás seguro de que deseas aprobar esta publicación?")
                .setPositiveButton("Sí, aprobar", (dialog, which) -> aprobarPublicacion())
                .setNegativeButton("Cancelar", null)
                .show();
        });
    }

    // -----------------------------  Tabs  -----------------------------
    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {              // Capítulos
                    chapterListContainer.setVisibility(View.VISIBLE);
                    detailsGroup.setVisibility(View.GONE);
                    loadChapters();
                } else {                                   // Detalles
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

    // --------------------------  Retrofit  -----------------------------
    private void loadSolicitudDetails() {
        api.getSolicitudById(idSolicitud).enqueue(new Callback<SolicitudDetalle>() {
            @Override
            public void onResponse(Call<SolicitudDetalle> call,
                                   Response<SolicitudDetalle> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SolicitudDetalle s = response.body();
                    tvTitle.setText("Título: " + s.getTitulo());
                    tvAuthor.setText("Autor(es): " + s.getAutores());
                    tvSynopsis.setText(s.getDescripcion());
                    tvPrice.setText("S/" + s.getPrecio_volumen());
                    if (s.getUrl_portada() != null && !s.getUrl_portada().isEmpty()) {
                        Picasso.get().load(s.getUrl_portada()).into(ivCover);
                    }
                } else {
                    Toast.makeText(getContext(),
                            "No se pudo cargar la solicitud",
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<SolicitudDetalle> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Error de red al cargar la solicitud",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChapters() {
        api.getChapters(idSolicitud).enqueue(new Callback<CapituloResponse>() {
            @Override
            public void onResponse(Call<CapituloResponse> call,
                                   Response<CapituloResponse> response) {
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().code == 0) {
                    List<String> chapters = response.body().chapters;
                    tvChapterHeader.setText("Capítulos (" + chapters.size() + ")");
                    chapterAdapter.actualizarDatos(chapters);
                } else {
                    Toast.makeText(getContext(),
                            "Error al cargar capítulos",
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<CapituloResponse> call, Throwable t) {
                Toast.makeText(getContext(),
                        "Fallo de conexión",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --------------------  Click en un capítulo  -----------------------
    private void onChapterClicked(String chapterName) {
        api.getChapterPages(idSolicitud, chapterName)
                .enqueue(new Callback<PaginaResponse>() {
                    @Override
                    public void onResponse(Call<PaginaResponse> call,
                                           Response<PaginaResponse> resp) {
                        if (resp.isSuccessful()
                                && resp.body() != null
                                && resp.body().code == 0) {
                            ArrayList<String> urls =
                                    new ArrayList<>(resp.body().pages);
                            Intent i = new Intent(getContext(),
                                    HistorietaReaderActivity.class);
                            i.putStringArrayListExtra("PAGES", urls);
                            startActivity(i);
                        } else {
                            Toast.makeText(getContext(),
                                    "Error al cargar páginas",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<PaginaResponse> call, Throwable t) {
                        Toast.makeText(getContext(),
                                "Fallo de conexión",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void aprobarPublicacion() {
        if (idSolicitud == 0) {
            Toast.makeText(getContext(), "ID de solicitud no válido", Toast.LENGTH_SHORT).show();
            return;
        }
        // Llamada a la API para aprobar publicación
        api.aprobarPublicacion(new AprobarPublicacionRequest(idSolicitud)).enqueue(new Callback<SolicitudResponse>() {
            @Override
            public void onResponse(Call<SolicitudResponse> call, Response<SolicitudResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Publicación aprobada", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                } else {
                    Toast.makeText(getContext(), "Error al aprobar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<SolicitudResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
