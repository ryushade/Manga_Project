package com.example.manga_project.Modelos;

public class SolicitudPublicacionRequest {
    private int id_user;
    private String tipo;
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


    public SolicitudPublicacionRequest() {}

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutores() {
        return autores;
    }

    public void setAutores(String autores) {
        this.autores = autores;
    }

    public String getAnio_publicacion() {
        return anio_publicacion;
    }

    public void setAnio_publicacion(String anio_publicacion) {
        this.anio_publicacion = anio_publicacion;
    }

    public String getPrecio_volumen() {
        return precio_volumen;
    }

    public void setPrecio_volumen(String precio_volumen) {
        this.precio_volumen = precio_volumen;
    }

    public String getRestriccion_edad() {
        return restriccion_edad;
    }

    public void setRestriccion_edad(String restriccion_edad) {
        this.restriccion_edad = restriccion_edad;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public String getGenero_principal() {
        return genero_principal;
    }

    public void setGenero_principal(String genero_principal) {
        this.genero_principal = genero_principal;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUrl_portada() {
        return url_portada;
    }

    public void setUrl_portada(String url_portada) {
        this.url_portada = url_portada;
    }

    public String getUrl_zip() {
        return url_zip;
    }

    public void setUrl_zip(String url_zip) {
        this.url_zip = url_zip;
    }
}
