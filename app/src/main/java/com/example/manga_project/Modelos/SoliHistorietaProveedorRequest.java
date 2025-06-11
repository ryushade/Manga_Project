package com.example.manga_project.Modelos;

import com.google.gson.annotations.SerializedName;

public class SoliHistorietaProveedorRequest {

    @SerializedName("id_solicitud")
    private int idSolicitud;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("estado")
    private String estado;

    @SerializedName("fecha_solicitud")
    private String fechaSolicitud;

    public SoliHistorietaProveedorRequest() { }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getEstado() {
        return estado;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }
}
