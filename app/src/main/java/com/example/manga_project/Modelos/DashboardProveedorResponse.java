package com.example.manga_project.Modelos;

import java.util.List;

public class DashboardProveedorResponse {
    public int code;
    public Stats stats;
    public List<PublicacionProveedor> publicaciones;

    public static class Stats {
        public int publicaciones_activas;
        public int solicitudes_pendientes;
    }
}

