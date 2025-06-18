package com.example.manga_project.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.manga_project.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.slider.Slider;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lector que no muestra el capítulo hasta que **todas** las páginas se han
 * descargado a la caché de Glide.
 */
public class HistorietaReaderActivity extends AppCompatActivity {

    // ─── Widgets principales ────────────────────────────────────────────
    private ViewPager2   vpPages;
    private Slider       sliderPages;
    private TextView     tvCurrent, tvTotal;
    private ImageButton  btnPrev, btnNext;

    // Overlay de precarga
    private FrameLayout  flPreload;
    private ProgressBar  pbPreload;
    private TextView     tvProgress;

    // Lista de URLs recibidas por Intent
    private List<String> pageUrls;

    // ─────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historieta_lector);

        pageUrls = getIntent().getStringArrayListExtra("PAGES");
        if (pageUrls == null) pageUrls = List.of();

        // FindViewById
        vpPages      = findViewById(R.id.vpPages);
        sliderPages  = findViewById(R.id.sliderPages);
        tvCurrent    = findViewById(R.id.tvCurrent);
        tvTotal      = findViewById(R.id.tvTotal);
        btnPrev      = findViewById(R.id.btnPrev);
        btnNext      = findViewById(R.id.btnNext);

        flPreload    = findViewById(R.id.flPreload);
        pbPreload    = findViewById(R.id.pbPreload);
        tvProgress   = findViewById(R.id.tvProgress);

        /**
         * FIX definitiva: deja el componente Slider en un rango dummy (0‒2)
         * hasta que sepamos cuántas páginas hay.  Así nunca se ejecuta el
         * layout con valueFrom == valueTo.
         */
        sliderPages.setValueFrom(0f);
        sliderPages.setValueTo(2f);
        sliderPages.setStepSize(1f);
        sliderPages.setValue(0f);
        sliderPages.setEnabled(false);
        tvCurrent    = findViewById(R.id.tvCurrent);
        tvTotal      = findViewById(R.id.tvTotal);
        btnPrev      = findViewById(R.id.btnPrev);
        btnNext      = findViewById(R.id.btnNext);

        flPreload    = findViewById(R.id.flPreload);
        pbPreload    = findViewById(R.id.pbPreload);
        tvProgress   = findViewById(R.id.tvProgress);

        /**
         * 💥 Parche inmediato:
         * Si sólo hay 0‑1 páginas, el slider de Material vampiro crashea porque
         * valueFrom == valueTo.  Lo reajustamos antes de que el layout se mida
         * (sucede después de onCreate), evitando la IllegalStateException.
         */
        if (pageUrls.size() < 2) {
            sliderPages.setValueFrom(0f);   // cualquier par distintos vale
            sliderPages.setValueTo(1f);
            sliderPages.setEnabled(false);  // además lo desactivamos
        }

        // Si hay overlay, mostrar progreso; si no, configurar lector directo
        if (flPreload != null) {
            flPreload.setVisibility(View.VISIBLE);
            pbPreload.setMax(pageUrls.size());
            tvProgress.setText("0 / " + pageUrls.size());
            new PreloadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            setupReader();
        }
    }

    // ─── Pre‑descarga en hilo de fondo ───────────────────────────────────
    private class PreloadTask extends AsyncTask<Void, Integer, Void> {
        private static final int THREADS = 4;
        private final ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        private int done;
        @Override protected Void doInBackground(Void... ignored) {
            for (String url : pageUrls) {
                pool.submit(() -> {
                    try {
                        Glide.with(getApplicationContext())
                                .downloadOnly()
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .load(url)
                                .submit()
                                .get();
                    } catch (Exception e) {
                        Log.e("PRELOAD", "Error " + url, e);
                    }
                    publishProgress(++done);
                });
            }
            pool.shutdown();
            while (!pool.isTerminated()) {
                try { Thread.sleep(40); } catch (InterruptedException e) {}
            }
            return null;
        }
        @Override protected void onProgressUpdate(Integer... v) {
            int n = v[0];
            if (pbPreload != null) pbPreload.setProgress(n);
            if (tvProgress != null) tvProgress.setText(n + " / " + pageUrls.size());
        }
        @Override protected void onPostExecute(Void r) { setupReader(); }
    }

    // ─── Configurar visor una vez todo descargado ───────────────────────
    private void setupReader() {
        if (flPreload != null) flPreload.setVisibility(View.GONE);
        if (pageUrls.isEmpty()) return;

        // ViewPager
        vpPages.setAdapter(new PageAdapter(pageUrls));
        vpPages.setOffscreenPageLimit(2);
        vpPages.registerOnPageChangeCallback(pageChangeCb);

        // Contadores
        tvTotal.setText(String.valueOf(pageUrls.size()));
        tvCurrent.setText("1");

        // Slider seguro: sólo si ≥2 páginas
        if (pageUrls.size() >= 2) {
            sliderPages.setEnabled(true);
            sliderPages.setValueFrom(1f);
            sliderPages.setValueTo(pageUrls.size());
            sliderPages.setStepSize(1f);
            sliderPages.setValue(1f);
            sliderPages.addOnChangeListener((s,v,f)->{
                if (f) vpPages.setCurrentItem(Math.round(v)-1,true);
            });
        } else {
            sliderPages.setEnabled(false);
        }

        // Navegación
        btnPrev.setOnClickListener(v->changePage(-1));
        btnNext.setOnClickListener(v->changePage(+1));
        updateNavButtons(0);

        // Tap para ocultar controles
        vpPages.setOnClickListener(v->toggleControls());
    }

    // ─── Navegación ──────────────────────────────────────────────────────
    private void changePage(int delta){
        int idx = vpPages.getCurrentItem()+delta;
        vpPages.setCurrentItem(Math.max(0,Math.min(idx,pageUrls.size()-1)),true);
    }

    private final ViewPager2.OnPageChangeCallback pageChangeCb = new ViewPager2.OnPageChangeCallback(){
        @Override public void onPageSelected(int pos){
            tvCurrent.setText(String.valueOf(pos+1));
            if (sliderPages.isEnabled()) sliderPages.setValue(pos+1);
            updateNavButtons(pos);
        }
    };

    private void updateNavButtons(int pos){
        btnPrev.setEnabled(pos>0);
        btnNext.setEnabled(pos<pageUrls.size()-1);
    }

    private boolean controlsVisible=true;
    private void toggleControls(){
        View overlay=findViewById(R.id.flOverlay);
        overlay.animate().alpha(controlsVisible?0f:1f).setDuration(200).start();
        controlsVisible=!controlsVisible;
    }

    // ─── Adapter interno ────────────────────────────────────────────────
    private static class PageAdapter extends RecyclerView.Adapter<PageAdapter.VH>{
        private final List<String> urls;
        PageAdapter(List<String> u){urls=u;}
        static class VH extends RecyclerView.ViewHolder{
            final PhotoView photo;
            VH(View v){super(v); photo = v.findViewById(R.id.photoView);} }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p,int v){
            View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_pagina,p,false);
            return new VH(view);
        }
        @Override public void onBindViewHolder(@NonNull VH h,int pos){
            Glide.with(h.photo.getContext())
                    .load(urls.get(pos))
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .fitCenter()
                    .into(h.photo);
        }
        @Override public int getItemCount(){return urls.size();}
    }
}
