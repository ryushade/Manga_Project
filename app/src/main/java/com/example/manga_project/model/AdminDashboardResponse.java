package com.example.manga_project.model;

import java.util.List;

public class AdminDashboardResponse {
    public int code;
    public Stats stats;
    public List<Grafico> grafico;
    public List<Reciente> recientes;

    public static class Stats {
        public int proveedores_pendientes;
        public int publicaciones_pendientes;
    }

    public static class Grafico {
        public String fecha;
        public int total;
    }

    public static class Reciente {
        public int id_solicitud;
        public String titulo;
        public String fecha_solicitud;
        public String email;
    }
}

