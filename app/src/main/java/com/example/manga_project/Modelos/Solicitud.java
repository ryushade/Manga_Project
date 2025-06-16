package com.example.manga_project.Modelos;

public class Solicitud {
    private final int    idSolicitud;
    private final String urlPortada;
    private final String titulo;
    private final String tipo;
    private final String autor;
    private final String contacto;
    private final String fecha;

    public Solicitud(int idSolicitud,
                     String urlPortada,
                     String titulo,
                     String tipo,
                     String autor,
                     String contacto,
                     String fecha) {
        this.idSolicitud = idSolicitud;
        this.urlPortada  = urlPortada;
        this.titulo      = titulo;
        this.tipo        = tipo;
        this.autor       = autor;
        this.contacto    = contacto;
        this.fecha       = fecha;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public String getUrlPortada() {
        return urlPortada;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getAutor() {
        return autor;
    }

    public String getContacto() {
        return contacto;
    }

    public String getFecha() {
        return fecha;
    }
}