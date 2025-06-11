package com.example.manga_project.Modelos;

public class Genero {
    private int id_genero;
    private String nombre_genero;
    private String tipo;

    public int getId_genero() {
        return id_genero;
    }

    public String getNombre_genero() {
        return nombre_genero;
    }

    public String getTipo() {
        return tipo;
    }

    public void setId_genero(int id_genero) {
        this.id_genero = id_genero;
    }

    public void setNombre_genero(String nombre_genero) {
        this.nombre_genero = nombre_genero;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return nombre_genero; // útil si usarás esto en un Spinner
    }
}
