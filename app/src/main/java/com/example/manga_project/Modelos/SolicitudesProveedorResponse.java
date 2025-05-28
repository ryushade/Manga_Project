package com.example.manga_project.Modelos;

import java.util.List;

public class SolicitudesProveedorResponse {
    private boolean success;
    private List<SolicitudesProveedorRequest> data;

    public boolean isSuccess() {
        return success;
    }

    public List<SolicitudesProveedorRequest> getData() {
        return data;
    }
}
