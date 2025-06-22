package com.example.manga_project.Modelos;

import java.util.List;

public class ListarCarritoResponse {
    public int code;
    public List<ItemCarrito> items;
    public double total;

    public static class ItemCarrito {
        public int id_volumen;
        public String titulo_volumen;
        public String historieta;
        public String portada_url;
        public int cantidad;
        public double precio_unit;
    }
}

