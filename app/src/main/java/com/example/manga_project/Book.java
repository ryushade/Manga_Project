package com.example.manga_project;

public class Book {
    private String title;
    private String author;
    private String description;
    private String imageUrl; // Para URL de imagen
    private int imageResId;   // Para recurso de imagen local
    private double price;

    // Constructor con `int` para imágenes locales
    public Book(String title, String author, String description, int imageResId, double price) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.imageResId = imageResId;
        this.price = price;
    }

    // Constructor con `String` para URL de imagen
    public Book(String title, String author, String description, String imageUrl, double price) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getImageUrl() {return imageUrl;}

    public double getPrice() { // Nuevo getter para el precio
        return price;
    }
}
