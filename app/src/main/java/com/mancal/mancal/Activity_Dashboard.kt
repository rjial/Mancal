package com.mancal.mancal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class Activity_Dashboard : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView3) as NavHostFragment
        val navController: NavController = navHostFragment.navController

        bottomNav.setupWithNavController(navController)
    }
    data class DataItem(var image: String?, var title: String?, var date: String?, var link: String?)
}