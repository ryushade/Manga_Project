package com.example.manga_project.Modelos;

public class SolicitudDetalle {
    private int id_solicitud;
    private String email;
    private String titulo;
    private String autores;
    private String anio_publicacion;
    private String precio_volumen;
    private String restriccion_edad;
    private String editorial;
    private String genero_principal;
    private String descripcion;
    private String url_portada;
    private String url_zip;
    private String fecha_solicitud;

    public int getId_solicitud() { return id_solicitud; }
    public String getEmail() { return email; }
    public String getTitulo() { return titulo; }
    public String getAutores() { return autores; }
    public String getAnio_publicacion() { return anio_publicacion; }
    public String getPrecio_volumen() { return precio_volumen; }
    public String getRestriccion_edad() { return restriccion_edad; }
    public String getEditorial() { return editorial; }
    public String getGenero_principal() { return genero_principal; }
    public String getDescripcion() { return descripcion; }
    public String getUrl_portada() { return url_portada; }
    public String getUrl_zip() { return url_zip; }
    public String getFecha_solicitud() { return fecha_solicitud; }

    // Setters opcionales si necesitas modificar los datos
}

