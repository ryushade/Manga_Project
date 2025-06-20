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

public class MainAdminActivity extends AppCompatActivity implements Logout {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        BottomNavigationView navView = findViewById(R.id.nav_view_admin);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_admin);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.dashboardFragment,
                R.id.solicitudesProveedorFragment,
                R.id.pedidosFragment,
                R.id.productosFragment,
                R.id.configuracionFragment
        ).build();

        NavigationUI.setupWithNavController(navView, navController);

        if (savedInstanceState == null) {
            navController.navigate(R.id.dashboardFragment);
        }
    }

    @Override
    public void logout() {
        // 1. Limpiar SharedPreferences
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        // 2. Volver a LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        // 3. Cerrar esta Activity
        finish();
    }
}
