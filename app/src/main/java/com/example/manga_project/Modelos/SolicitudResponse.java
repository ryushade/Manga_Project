package com.example.manga_project.Modelos;

import com.google.gson.annotations.SerializedName;

public class SolicitudResponse {
    @SerializedName("msg")
    private String msg;

    public String getMsg() {
        return msg;
    }
}
