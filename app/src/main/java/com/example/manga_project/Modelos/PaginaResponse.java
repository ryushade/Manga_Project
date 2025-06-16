package com.example.manga_project.Modelos;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaginaResponse {
    @SerializedName("code")  public int    code;
    @SerializedName("pages") public List<String> pages;
}
