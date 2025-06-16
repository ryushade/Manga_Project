package com.example.manga_project.Modelos;

public class PublicacionItem {
    private int IdSolicitud;
    private String titulo;
    private String tipo;
    private String autor;
    private String correoContacto;
    private String fechaSolicitud;
    private String urlPortada;

    // Constructor
    public PublicacionItem(int IdSolicitud, String titulo, String tipo, String autor, String correoContacto, String fechaSolicitud, String urlPortada) {
        this.titulo = titulo;
        this.tipo = tipo;
        this.autor = autor;
        this.correoContacto = correoContacto;
        this.fechaSolicitud = fechaSolicitud;
        this.urlPortada = urlPortada;
    }

    // Getters
    public String getTitulo() {
        return titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getAutor() {
        return autor;
    }

    public String getCorreoContacto() {
        return correoContacto;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public String getUrlPortada() {
        return urlPortada;
    }

    public int getIdSolicitud() {
        return IdSolicitud;
    }
}
