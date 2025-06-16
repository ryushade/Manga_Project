package com.example.manga_project.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import androidx.recyclerview.widget.RecyclerView;

import com.example.manga_project.R;
import com.google.android.material.slider.Slider;
import com.bumptech.glide.Glide;

import java.util.List;

public class HistorietaReaderActivity extends AppCompatActivity {

    private ViewPager2 vpPages;
    private Slider sliderPages;
    private TextView tvCurrent, tvTotal;
    private ImageButton btnPrev, btnNext;
    private List<String> pageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historieta_lector);

        // Obtener lista de URLs desde el Intent
        pageUrls = getIntent().getStringArrayListExtra("PAGES");
        if (pageUrls == null) pageUrls = List.of();

        // Referencias a vistas
        vpPages = findViewById(R.id.vpPages);
        sliderPages = findViewById(R.id.sliderPages);
        tvCurrent = findViewById(R.id.tvCurrent);
        tvTotal = findViewById(R.id.tvTotal);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        // Configurar ViewPager2
        vpPages.setAdapter(new PageAdapter(pageUrls));
        vpPages.registerOnPageChangeCallback(pageChangeCallback);

        // Configurar Slider dinámico
        sliderPages.setValueFrom(1f);
        sliderPages.setValueTo(pageUrls.size());
        sliderPages.setStepSize(1f);
        sliderPages.setValue(1f);
        tvTotal.setText(String.valueOf(pageUrls.size()));
        tvCurrent.setText("1");

        sliderPages.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                int page = Math.round(value) - 1;
                vpPages.setCurrentItem(page, true);
            }
        });

        // Botones Prev/Next
        btnPrev.setOnClickListener(v -> changePage(-1));
        btnNext.setOnClickListener(v -> changePage(+1));

        // Tap en la página para mostrar/ocultar controles
        vpPages.setOnClickListener(v -> toggleControls());
    }

    private void changePage(int delta) {
        int index = vpPages.getCurrentItem() + delta;
        index = Math.max(0, Math.min(index, pageUrls.size() - 1));
        vpPages.setCurrentItem(index, true);
    }

    private boolean controlsVisible = true;
    private void toggleControls() {
        View overlay = findViewById(R.id.flOverlay);
        overlay.animate()
                .alpha(controlsVisible ? 0f : 1f)
                .setDuration(200)
                .start();
        controlsVisible = !controlsVisible;
    }

    private final ViewPager2.OnPageChangeCallback pageChangeCallback =
            new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    int page = position + 1;
                    tvCurrent.setText(String.valueOf(page));
                    sliderPages.setValue(page);
                }
            };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vpPages.unregisterOnPageChangeCallback(pageChangeCallback);
    }

    // Adapter interno
    private static class PageAdapter extends RecyclerView.Adapter<PageAdapter.PageVH> {
        private final List<String> urls;
        PageAdapter(List<String> urls) { this.urls = urls; }

        static class PageVH extends RecyclerView.ViewHolder {
            com.github.chrisbanes.photoview.PhotoView photoView;
            PageVH(@NonNull View itemView) {
                super(itemView);
                photoView = itemView.findViewById(R.id.photoView);
            }
        }

        @NonNull
        @Override
        public PageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pagina, parent, false);
            return new PageVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PageVH holder, int position) {
            String url = urls.get(position);
            Glide.with(holder.photoView.getContext())
                    .load(url)
                    .placeholder(android.R.color.black)
                    .into(holder.photoView);
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }
    }
}
