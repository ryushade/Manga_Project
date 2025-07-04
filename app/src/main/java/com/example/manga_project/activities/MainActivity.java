package com.example.manga_project.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Logout;
import com.example.manga_project.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements Logout {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ApiClient.setContext(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configura la Toolbar como el ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Usa una Toolbar de androidx.appcompat.widget.Toolbar

        // Configura el BottomNavigationView
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Configura el NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment, R.id.searchFragment, R.id.libraryFragment, R.id.userFragment)
                .build();
        NavigationUI.setupWithNavController(navView, navController);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);


        // Configura el botón de la tienda para abrir CartActivity
        ImageButton shopButton = findViewById(R.id.shop_button);
        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
                // que no se vea en userFragment

            }
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Oculta el BottomNavigationView en UserFragment
            if (destination.getId() == R.id.userFragment || destination.getId() == R.id.libraryFragment) {
                shopButton.setVisibility(View.GONE);
                toolbar.setVisibility(View.GONE);
            } else {
                shopButton.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);
            }
        });



//        // Configura el botón de perfil en la AppBar para mostrar un PopupMenu
//        ImageButton profileButton = findViewById(R.id.profile_button);
//        profileButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showPopupMenu(view);
//            }
//        });


    }


    // Método para cerrar sesión
    public void logout() {
        // Eliminar el token de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("token");
        editor.apply();

        // Redirigir al usuario a LoginActivity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Cierra la actividad actual
    }


}
