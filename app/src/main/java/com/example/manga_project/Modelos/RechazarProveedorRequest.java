package com.example.manga_project.Modelos;

import com.google.gson.annotations.SerializedName;

public class RechazarProveedorRequest {
    @SerializedName("id_user")
    private final int idUser;

    public RechazarProveedorRequest(int idUser) {
        this.idUser = idUser;
    }

    public int getIdUser() {
        return idUser;
    }
}
