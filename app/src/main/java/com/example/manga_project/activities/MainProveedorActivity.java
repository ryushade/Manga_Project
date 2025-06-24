package com.example.manga_project.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.manga_project.Logout;
import com.example.manga_project.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainProveedorActivity extends AppCompatActivity implements Logout {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_proveedor);

        // Inicializar ApiClient con el contexto de la aplicación
        com.example.manga_project.Api_cliente.ApiClient.setContext(getApplicationContext());

        BottomNavigationView navView = findViewById(R.id.nav_view_proveedor);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_proveedor);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.inicioFragment, R.id.publicarFragment, R.id.pedidosFragment, R.id.misSolicitudes, R.id.configFragment
        ).build();

        NavigationUI.setupWithNavController(navView, navController);

        if (savedInstanceState == null) {
            navController.navigate(R.id.inicioFragment);
        }
    }

    public void logout() {
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        prefs.edit().remove("token").apply();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
