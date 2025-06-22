package com.example.manga_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manga_project.Modelos.ListarCarritoResponse.ItemCarrito;
import com.example.manga_project.adapters.ItemUsuarioAdapter;
import com.example.manga_project.R;
import java.util.ArrayList;
import java.util.List;

public class DetalleOrdenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_orden);

        TextView tvDetalle = findViewById(R.id.tvDetalleOrden);
        Button btnVolver = findViewById(R.id.btnVolverInicio);
        RecyclerView rvHistorietas = findViewById(R.id.rvHistorietasOrden);

        // Aquí puedes mostrar detalles de la orden, por ahora es un mensaje genérico
        tvDetalle.setText("¡Gracias por tu compra!\n\nTu orden ha sido procesada correctamente.\nPuedes ver tus historietas en la sección 'Mi librería'.");

        // Recibe la lista de historietas por intent
        List<ItemCarrito> historietas = (List<ItemCarrito>) getIntent().getSerializableExtra("historietas");
        if (historietas == null) historietas = new ArrayList<>();
        ItemUsuarioAdapter adapter = new ItemUsuarioAdapter();
        rvHistorietas.setLayoutManager(new LinearLayoutManager(this));
        rvHistorietas.setAdapter(adapter);
        // Adaptar los datos de ItemCarrito a ItemUsuario para el adapter
        List<com.example.manga_project.Modelos.ItemUsuario> items = new ArrayList<>();
        for (ItemCarrito item : historietas) {
            com.example.manga_project.Modelos.ItemUsuario iu = new com.example.manga_project.Modelos.ItemUsuario();
            iu.titulo = item.titulo;
            iu.portada = item.portada;
            iu.autores = item.autores;
            items.add(iu);
        }
        adapter.setItems(items, false);

        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(DetalleOrdenActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
