package com.example.manga_project;

import com.example.manga_project.Modelos.VolumenResponse;
import java.util.List;

public class SectionVolumen {
    private String title;
    private List<VolumenResponse> volumenes;

    public SectionVolumen(String title, List<VolumenResponse> volumenes) {
        this.title = title;
        this.volumenes = volumenes;
    }

    public String getTitle() {
        return title;
    }

    public List<VolumenResponse> getVolumenes() {
        return volumenes;
    }
}

