package com.example.ebook_proyect.Modelos;

public class RegisterRequest {
    private String email_user;
    private String pass_user;
    private String dni_lec;
    private String nom_lec;
    private String apellidos_lec; // Campo para un solo apellido
    private String fecha_nac;



    public RegisterRequest(String email_lec, String password, String dni_lec, String nom_lec, String apellido_lec, String fecha_nac) {
        this.email_user = email_lec;
        this.pass_user = password;
        this.dni_lec = dni_lec;
        this.nom_lec = nom_lec;
        this.apellidos_lec = apellido_lec; // Campo modificado
        this.fecha_nac = fecha_nac;
    }

    // Getters y setters si son necesarios
}