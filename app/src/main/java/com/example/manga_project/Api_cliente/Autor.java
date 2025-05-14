package com.example.manga_project.Api_cliente;

public class Autor {
    private int id_aut;
    private String nom_aut;
    private String apePat_aut;
    private String apeMat_aut;

    // Constructor vacío
    public Autor() {
    }

    // Getters y Setters
    public int getId_aut() {
        return id_aut;
    }

    public void setId_aut(int id_aut) {
        this.id_aut = id_aut;
    }

    public String getNom_aut() {
        return nom_aut;
    }

    public void setNom_aut(String nom_aut) {
        this.nom_aut = nom_aut;
    }

    public String getApePat_aut() {
        return apePat_aut;
    }

    public void setApePat_aut(String apePat_aut) {
        this.apePat_aut = apePat_aut;
    }

    public String getApeMat_aut() {
        return apeMat_aut;
    }

    public void setApeMat_aut(String apeMat_aut) {
        this.apeMat_aut = apeMat_aut;
    }
}
