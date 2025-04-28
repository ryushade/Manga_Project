package com.example.ebook_proyect.Api_cliente;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AutoresApiResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("msg")
    private String message;

    @SerializedName("autores")
    private List<Autor> autores;

    @SerializedName("libros")
    private List<Libro> libros;

    // Constructor vacío
    public AutoresApiResponse() {
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

    public List<Autor> getAutores() {
        return autores;
    }

    public void setAutores(List<Autor> autores) {
        this.autores = autores;
    }

    public List<Libro> getLibros() {
        return libros;
    }

    public void setLibros(List<Libro> libros) {
        this.libros = libros;
    }
}
