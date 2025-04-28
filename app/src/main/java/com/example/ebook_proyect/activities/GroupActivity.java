package com.example.ebook_proyect.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ebook_proyect.R;
import com.google.android.material.button.MaterialButton;

public class GroupActivity extends AppCompatActivity {


    private MaterialButton backButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backButton = findViewById(R.id.backButton);


        backButton.setOnClickListener(v -> finish());
    }
}