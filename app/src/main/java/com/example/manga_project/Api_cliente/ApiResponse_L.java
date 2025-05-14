package com.example.manga_project.Api_cliente;

import com.google.gson.annotations.SerializedName;

public class ApiResponse_L {
    @SerializedName("code")
    private int code;

    @SerializedName("msg")
    private String message;

    @SerializedName("data")
    private Libro data; // Un solo objeto Libro, no una lista

    // Constructor vacío
    public ApiResponse_L() {
    }

    // Getters y Setters
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Libro getData() {
        return data;
    }

    public void setData(Libro data) {
        this.data = data;
    }
}
