package com.example.manga_project.Modelos;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private String message;

    // Mapea el campo "access_token" al atributo "token"
    @SerializedName("access_token")
    private String token;

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }
}
