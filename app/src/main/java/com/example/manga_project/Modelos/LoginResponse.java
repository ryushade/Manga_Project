package com.example.manga_project.Modelos;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private String message;

    @SerializedName("access_token")
    private String token;

    @SerializedName("id_rol")
    private int idRol;  // <-- nuevo campo para recibir el rol

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public int getIdRol() {
        return idRol;
    }
}
