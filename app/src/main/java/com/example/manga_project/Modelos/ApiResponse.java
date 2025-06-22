package com.example.manga_project.Modelos;

import java.util.List;

public class ApiResponse<T> {
    public boolean success;
    public String type;
    public List<ItemUsuario> data;
}

