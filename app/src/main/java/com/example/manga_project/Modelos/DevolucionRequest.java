package com.example.manga_project.Modelos;

public class DevolucionRequest {
    private int id_ven;
    private String motivo;

    public DevolucionRequest(int id_ven, String motivo) {
        this.id_ven = id_ven;
        this.motivo = motivo;
    }

    // Getters y Setters
    public int getId_ven() {
        return id_ven;
    }

    public void setId_ven(int id_ven) {
        this.id_ven = id_ven;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
