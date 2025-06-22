package com.example.manga_project.Modelos;

public class AprobarPublicacionRequest {
    private int id_solicitud;

    public AprobarPublicacionRequest(int id_solicitud) {
        this.id_solicitud = id_solicitud;
    }

    public int getId_solicitud() {
        return id_solicitud;
    }

    public void setId_solicitud(int id_solicitud) {
        this.id_solicitud = id_solicitud;
    }
}

