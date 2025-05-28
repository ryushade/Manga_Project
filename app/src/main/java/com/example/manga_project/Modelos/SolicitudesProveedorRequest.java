package com.example.manga_project.Modelos;

public class SolicitudesProveedorRequest {
    private int id_user;
    private String email;
    private String nombre;
    private String fecha_solicitud;

    public int getId_user() {
        return id_user;
    }

    public String getEmail() {
        return email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getFecha_solicitud() {
        return fecha_solicitud;
    }
}