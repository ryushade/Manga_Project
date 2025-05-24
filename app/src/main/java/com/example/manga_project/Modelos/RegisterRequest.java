package com.example.manga_project.Modelos;

public class RegisterRequest {
    private String email_user;
    private String pass_user;
    private String dni_lec;
    private String nom_lec;
    private String apellidos_lec;
    private String fecha_nac;
    private boolean proveedor_solicitud;
    private int id_rol;

    public RegisterRequest(String email_user, String pass_user, String dni_lec, String nom_lec,
                           String apellidos_lec, String fecha_nac,
                           boolean proveedor_solicitud, int id_rol) {
        this.email_user = email_user;
        this.pass_user = pass_user;
        this.dni_lec = dni_lec;
        this.nom_lec = nom_lec;
        this.apellidos_lec = apellidos_lec;
        this.fecha_nac = fecha_nac;
        this.proveedor_solicitud = proveedor_solicitud;
        this.id_rol = id_rol;
    }

    public String getEmail_user() { return email_user; }
    public String getPass_user() { return pass_user; }
    public boolean isProveedor_solicitud() { return proveedor_solicitud; }
    public int getId_rol() { return id_rol; }
}
