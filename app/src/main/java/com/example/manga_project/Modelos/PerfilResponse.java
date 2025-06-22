package com.example.manga_project.Modelos;

public class PerfilResponse {
    private int id_user;
    private String nombre;
    private String email;

    private int id_rol;
    private boolean proveedor_solicitud;  // Campo adicional

    public PerfilResponse() {}
    public int getId_user() { return id_user; }
    public void setId_user(int id_user) { this.id_user = id_user; }
    public int getId_rol() {
        return id_rol;
    }

    public void setId_rol(int id_rol) {
        this.id_rol = id_rol;
    }

    // Getters y setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isProveedor_solicitud() {
        return proveedor_solicitud;
    }

    public void setProveedor_solicitud(boolean proveedor_solicitud) {
        this.proveedor_solicitud = proveedor_solicitud;
    }
}
