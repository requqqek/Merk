package com.example.merk

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.merk.fragments.AssignmentsFragment
import com.example.merk.fragments.ProfileFragment
import com.example.merk.fragments.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val prefs = getSharedPreferences("merk_session", MODE_PRIVATE)
        findViewById<TextView>(R.id.tvUsername).text = prefs.getString("login", "")

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) loadFragment(AssignmentsFragment())

        bottomNav.setOnItemSelectedListener {
            loadFragment(when (it.itemId) {
                R.id.nav_assignments -> AssignmentsFragment()
                R.id.nav_profile -> ProfileFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> AssignmentsFragment()
            })
            true
        }
    }

    private fun loadFragment(f: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, f).commit()
    }
}
