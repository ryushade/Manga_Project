package com.example.manga_project.Api_cliente;

public class Libro {
    private String isbn_lib;
    private String titulo_lib;
    private int anoPub_lib;
    private String image; // URL de la imagen
    private String descripcion;
    private String nombre_completo;
    private String bookPDF;
    private float precio_lib;

    public Libro(String isbn_lib, String titulo_lib, int anoPub_lib, String image, String descripcion, String nombre_completo, String bookPDF, float precio_lib) {
        this.isbn_lib = isbn_lib;
        this.titulo_lib = titulo_lib;
        this.anoPub_lib = anoPub_lib;
        this.image = image;
        this.descripcion = descripcion;
        this.nombre_completo = nombre_completo;
        this.precio_lib = precio_lib;
    }

    public String getIsbn_lib() {
        return isbn_lib;
    }

    public void setIsbn_lib(String isbn_lib) {
        this.isbn_lib = isbn_lib;
    }

    public String getTitulo_lib() {
        return titulo_lib;
    }

    public void setTitulo_lib(String titulo_lib) {
        this.titulo_lib = titulo_lib;
    }

    public int getAnoPub_lib() {
        return anoPub_lib;
    }

    public void setAnoPub_lib(int anoPub_lib) {
        this.anoPub_lib = anoPub_lib;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNombre_completo() {
        return nombre_completo;
    }

    public void setNombre_completo(String nombre_completo) {
        this.nombre_completo = nombre_completo;
    }

    public float getPrecio_lib() {
        return precio_lib;
    }

    public void setPrecio_lib(float precio_lib) {
        this.precio_lib = precio_lib;
    }

    public String getBookPDF() {
        return bookPDF;
    }

    public void setBookPDF(String bookPDF) {
        this.bookPDF = bookPDF;
    }
}


