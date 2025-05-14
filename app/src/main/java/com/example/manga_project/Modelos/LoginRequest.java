package com.example.manga_project.Modelos;

public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest(String email_lec, String password) {
        this.email = email_lec;
        this.password = password;
    }
}
