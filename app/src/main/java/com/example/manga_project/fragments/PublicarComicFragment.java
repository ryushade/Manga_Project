package com.example.manga_project.fragments;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.Genero;
import com.example.manga_project.Modelos.SolicitudPublicacionRequest;
import com.example.manga_project.Modelos.SolicitudPublicacionResponse;
import com.example.manga_project.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublicarComicFragment extends Fragment {

    private ImageView imgCover;
    private TextView tvPdfName;
    private Spinner spGenero;
    private EditText etTitle, etAuthors, etYear, etPrice, etIssue, etEditorial, etDetails;

    private String urlPortada, urlZip;

    private AuthService api;

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> pickZipLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_register_comic, container, false);
        api = ApiClient.getClientConToken().create(AuthService.class);
        mapearUi(v);
        configurarLaunchers();
        cargarGeneros();
        return v;
    }

    private void mapearUi(View v) {
        v.findViewById(R.id.btnBack).setOnClickListener(btn -> requireActivity().getSupportFragmentManager().popBackStack());

        imgCover    = v.findViewById(R.id.imgComicCover);
        tvPdfName   = v.findViewById(R.id.tvPdfFileName);
        spGenero    = v.findViewById(R.id.spGenero);
        etTitle     = v.findViewById(R.id.etTitle);
        etAuthors   = v.findViewById(R.id.etAuthors);
        etYear      = v.findViewById(R.id.etYear);
        etPrice     = v.findViewById(R.id.etPrecio);
        etIssue     = v.findViewById(R.id.etIssueNumber);
        etEditorial = v.findViewById(R.id.etEditorial);
        etDetails   = v.findViewById(R.id.etDetails);

        v.findViewById(R.id.btnChooseImage).setOnClickListener(btn -> pickImageLauncher.launch("image/*"));
        v.findViewById(R.id.btnUploadPdf).setOnClickListener(btn -> pickZipLauncher.launch("application/zip"));

        ((Button) v.findViewById(R.id.btnPublish)).setOnClickListener(btn -> publicarComic());
    }

    private void configurarLaunchers() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) subirArchivo(uri, "image/*", true);
        });

        pickZipLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                tvPdfName.setText(nombreDeArchivo(requireContext(), uri));
                subirArchivo(uri, "application/zip", false);
            }
        });
    }

    private void cargarGeneros() {
        api.obtenerGeneros("comic").enqueue(new Callback<List<Genero>>() {
            @Override
            public void onResponse(@NonNull Call<List<Genero>> call, @NonNull Response<List<Genero>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArrayAdapter<Genero> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, response.body());
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spGenero.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "Error al cargar géneros", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Genero>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subirArchivo(Uri uri, String mime, boolean esPortada) {
        File archivo = copiarAFileTemporal(requireContext(), uri);
        if (archivo == null) {
            Toast.makeText(getContext(), "No se pudo leer el archivo", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(MediaType.parse(mime), archivo);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", archivo.getName(), body);

        Call<ResponseBody> call = esPortada ? api.subirPortada(part) : api.subirZip(part);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(), "Error al subir archivo (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    String json = response.body().string();
                    String url = new org.json.JSONObject(json).getString("url");
                    if (esPortada) {
                        urlPortada = url;
                        Picasso.get().load(url).into(imgCover);
                    } else {
                        urlZip = url;
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Respuesta inesperada del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void publicarComic() {
        if (urlPortada == null || urlZip == null) {
            Toast.makeText(getContext(), "Sube portada y ZIP primero", Toast.LENGTH_SHORT).show();
            return;
        }

        String titulo = etTitle.getText().toString().trim();
        String autores = etAuthors.getText().toString().trim();
        if (titulo.isEmpty() || autores.isEmpty()) {
            Toast.makeText(getContext(), "Título y autores obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        Genero generoSeleccionado = (Genero) spGenero.getSelectedItem();

        SolicitudPublicacionRequest data = new SolicitudPublicacionRequest();
        data.setId_user(2); // temporal
        data.setTipo("comic");
        data.setTitulo(titulo);
        data.setAutores(autores);
        data.setAnio_publicacion(etYear.getText().toString().trim());
        data.setPrecio_volumen(etPrice.getText().toString().trim());
        data.setRestriccion_edad(etIssue.getText().toString().trim());
        data.setEditorial(etEditorial.getText().toString().trim());
        data.setGenero_principal(generoSeleccionado.getNombre_genero());
        data.setDescripcion(etDetails.getText().toString().trim());
        data.setUrl_portada(urlPortada);
        data.setUrl_zip(urlZip);

        new AlertDialog.Builder(getContext())
            .setTitle("Confirmar publicación")
            .setMessage("¿Estás seguro de que deseas enviar tu solicitud de publicación de este cómic?")
            .setPositiveButton("Sí", (dialog, which) -> {
                api.registrarSolicitud(data).enqueue(new Callback<SolicitudPublicacionResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<SolicitudPublicacionResponse> call, @NonNull Response<SolicitudPublicacionResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Solicitud enviada correctamente", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(getContext(), "Error al registrar (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SolicitudPublicacionResponse> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("No", null)
            .show();
    }

    @Nullable
    private static File copiarAFileTemporal(Context ctx, Uri uri) {
        try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
            if (in == null) return null;
            String nombre = nombreDeArchivo(ctx, uri);
            File tmp = File.createTempFile("tmp_", "_" + nombre, ctx.getCacheDir());
            try (FileOutputStream out = new FileOutputStream(tmp)) {
                byte[] buffer = new byte[4096];
                int n;
                while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
            }
            return tmp;
        } catch (Exception e) {
            return null;
        }
    }

    private static String nombreDeArchivo(Context ctx, Uri uri) {
        String nombre = "archivo";
        Cursor c = ctx.getContentResolver().query(uri, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx != -1) nombre = c.getString(idx);
            }
            c.close();
        }
        return nombre;
    }
}
