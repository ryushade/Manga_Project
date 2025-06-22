package com.example.manga_project.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.manga_project.Logout;
import com.example.manga_project.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainAdminActivity extends AppCompatActivity implements Logout {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        // 1) Obtén NavController y BottomNavigationView
        NavController navController = Navigation.findNavController(
                this, R.id.nav_host_fragment_content_main_admin);
        BottomNavigationView navView = findViewById(R.id.nav_view_admin);

        // 2) Configura los destinos de AppBarConfiguration (si usas Toolbar)
        AppBarConfiguration appBarConfig = new AppBarConfiguration.Builder(
                R.id.dashboardFragment,
                R.id.solicitudesProveedorFragment,
                R.id.pedidosFragment,
                R.id.productosFragment,
                R.id.configuracionFragment
        ).build();

        // 3) Conecta BottomNav y (opcional) ActionBar/Toolbar
        NavigationUI.setupWithNavController(navView, navController);
        // Si tuvieras un Toolbar:
        // NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);

        // 4) Solo la primera vez, navega a dashboardFragment y limpia el backstack
        if (savedInstanceState == null) {
            NavOptions options = new NavOptions.Builder()
                    // limpia el fragmento inicial (homeFragment) del backstack
                    .setPopUpTo(navController.getGraph().getStartDestination(), true)
                    .build();

            navController.navigate(R.id.dashboardFragment, null, options);
        }
    }

    @Override
    public void logout() {
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
