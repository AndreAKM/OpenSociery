package com.example.opensociety

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.opensociety.databinding.ActivityMainBinding
import android.content.Intent
import com.example.opensociety.connection.FoneClientService
import android.os.Build
import android.util.Log
import com.example.opensociety.connection.ServiceCommandBuilder


class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_contacts_list))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        var intentService = ServiceCommandBuilder(this)
            .setCommand(ServiceCommandBuilder.Command.START).build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "start foreground service")
            startForegroundService(intentService)
        } else {
            startService(intentService)
        }
    }

    override fun onSupportNavigateUp()
            = findNavController(R.id.nav_host_fragment_activity_main).navigateUp()
}