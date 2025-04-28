package com.example.ebook_proyect.activities;

import com.example.ebook_proyect.R;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;

public class Valoration extends AppCompatActivity {

    private float ratingValue;
    private RatingBar ratingBar;
    private TextView bookTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description_book);

        ratingBar = findViewById(R.id.ratingBar);
        bookTitleTextView = findViewById(R.id.textViewTitle2);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String emailUser = sharedPreferences.getString("email", "");


        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            ratingValue = rating;
            String bookTitle = bookTitleTextView.getText().toString(); // Obtén el título del libro

            Toast.makeText(Valoration.this, "Valoración de '" + bookTitle + "': " + ratingValue + " estrellas", Toast.LENGTH_SHORT).show();

            // Llamar al método para enviar los datos a la API
            enviarValoracion(emailUser, bookTitle, ratingValue);
        });
    }
    private void enviarValoracion(String emailUser, String bookTitle, float valoracion) {

    }
}
