package com.example.manga_project.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.manga_project.R;
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