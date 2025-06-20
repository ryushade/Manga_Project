package com.example.manga_project;

import java.util.List;

public class Section {
    private String title;
    private List<Libro> libros;

    public Section(String title, List<Libro> libros) {
        this.title = title;
        this.libros = libros;
    }

    public String getTitle() {
        return title;
    }

    public List<Libro> getLibros() {
        return libros;
    }
}
