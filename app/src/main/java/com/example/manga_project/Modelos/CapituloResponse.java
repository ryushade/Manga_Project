package com.example.manga_project.Modelos;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CapituloResponse {
    @SerializedName("code")     public int    code;
    @SerializedName("chapters") public List<String> chapters;
}


