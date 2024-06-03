package com.example.tracking_ble

import android.os.Bundle
import android.view.Menu
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuCompat
import com.example.tracking_ble.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // Configuration de la barre d'application
    private lateinit var appBarConfiguration: AppBarConfiguration

    // Liaison de l'activité principale
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation de la liaison avec le layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Définir la barre d'outils en tant que barre d'action
        setSupportActionBar(binding.appBarMain.toolbar)

        // Initialisation du tiroir de navigation et de la vue de navigation
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Configuration de la barre d'application avec les destinations de navigation
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_entrepot, R.id.nav_camion, R.id.nav_parametres
            ), drawerLayout
        )

        // Configurer la barre d'action avec le contrôleur de navigation
        setupActionBarWithNavController(navController, appBarConfiguration)
        // Configurer la vue de navigation avec le contrôleur de navigation
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Gonfler le menu; cela ajoute des éléments à la barre d'action si elle est présente
        menuInflater.inflate(R.menu.main, menu)

        // Activer le séparateur de groupe dans le menu
        MenuCompat.setGroupDividerEnabled(menu, true)

        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        // Gestion de la navigation de retour avec le contrôleur de navigation
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
