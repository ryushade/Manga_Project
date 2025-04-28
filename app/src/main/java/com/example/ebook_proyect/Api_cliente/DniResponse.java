package com.example.ebook_proyect.Api_cliente;

public class DniResponse {
    private boolean success;
    private String dni;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String codVerifica;

    // Getters y Setters
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidoPaterno + " " + apellidoMaterno; }
    // Otros getters y setters según sea necesario
}
