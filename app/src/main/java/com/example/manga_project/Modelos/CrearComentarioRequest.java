package com.example.manga_project.Modelos;// ...existing code...
import com.google.gson.annotations.SerializedName;

public class CrearComentarioRequest {
    @SerializedName("id_historieta")
    public int idHistorieta;
    @SerializedName("comentario")
    public String comentario;

    public CrearComentarioRequest(int idHistorieta, String comentario) {
        this.idHistorieta = idHistorieta;
        this.comentario = comentario;
    }
}
// ...existing code...
