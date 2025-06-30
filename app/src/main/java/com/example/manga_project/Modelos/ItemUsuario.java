package com.example.manga_project.Modelos;

public class ItemUsuario {
    public int id;
    public String portada;
    public String titulo;
    public String autores;
    public String fecha;
    public boolean esWishlist = false;
    public int id_volumen;
    public int id_venta; // Necesario para las devoluciones
    public boolean puedeDevolver = true; // Para controlar si se muestra el botón de devolución

    // Campo simple para verificar si ya fue devuelto exitosamente
    public String estadoDevolucion = null; // Solo necesitamos verificar si es "succeeded"
}
